/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.derby;


import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.TransactionSynchronizationRegistry;

import com.ibm.fhir.database.utils.api.IDatabaseTranslator;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.persistence.jdbc.FHIRPersistenceJDBCCache;
import com.ibm.fhir.persistence.jdbc.connection.FHIRDbFlavor;
import com.ibm.fhir.persistence.jdbc.dao.api.IResourceReferenceDAO;
import com.ibm.fhir.persistence.jdbc.dao.api.JDBCIdentityCache;
import com.ibm.fhir.persistence.jdbc.dao.api.ParameterDAO;
import com.ibm.fhir.persistence.jdbc.dao.api.ResourceIndexRecord;
import com.ibm.fhir.persistence.jdbc.dao.impl.JDBCIdentityCacheImpl;
import com.ibm.fhir.persistence.jdbc.dao.impl.ParameterVisitorBatchDAO;
import com.ibm.fhir.persistence.jdbc.dao.impl.ResourceDAOImpl;
import com.ibm.fhir.persistence.jdbc.dto.ExtractedParameterValue;

/**
 * DAO used to contain the logic required to reindex a given resource
 */
public class ReindexResourceDAO extends ResourceDAOImpl {
    private static final Logger logger = Logger.getLogger(ReindexResourceDAO.class.getName());
    private static final String CLASSNAME = ReindexResourceDAO.class.getSimpleName();
    private static final SecureRandom random = new SecureRandom();

    // The translator specific to the database type we're working with
    private final IDatabaseTranslator translator;
    
    private final ParameterDAO parameterDao;
    
    /**
     * 
     * @param connection
     * @param translator
     * @param schemaName
     * @param flavor
     * @param cache
     * @param rrd
     */
    public ReindexResourceDAO(Connection connection, IDatabaseTranslator translator, ParameterDAO parameterDao, String schemaName, FHIRDbFlavor flavor, FHIRPersistenceJDBCCache cache, IResourceReferenceDAO rrd) {
        super(connection, schemaName, flavor, cache, rrd);
        this.translator = translator;
        this.parameterDao = parameterDao;
    }

    /**
     * @param strat the connection strategy
     * @param trxSynchRegistry
     */
    public ReindexResourceDAO(Connection connection, IDatabaseTranslator translator, ParameterDAO parameterDao, String schemaName, FHIRDbFlavor flavor, TransactionSynchronizationRegistry trxSynchRegistry, FHIRPersistenceJDBCCache cache, IResourceReferenceDAO rrd) {
        super(connection, schemaName, flavor, trxSynchRegistry, cache, rrd);
        this.translator = translator;
        this.parameterDao = parameterDao;
    }

    /**
     * Mark a list of resources to reindex and return them
     * @param reindexTstamp
     * @param resourceCount
     * @return
     * @throws Exception
     */
    public List<ResourceIndexRecord> getResourcesToReindex(Instant reindexTstamp, int resourceCount) throws Exception {
        List<ResourceIndexRecord> result = new ArrayList<>();
        
        // no need to close
        Connection connection = getConnection();

        // logger.info("Current schema: " + connection.getSchema());

        // Get the next sequence value we'll use to mark the resources we want to process
        // final String selectNextValue = "VALUES(NEXT VALUE FOR reindex_seq)";
        final String selectNextValue = translator.selectSequenceNextValue(getSchemaName(), "reindex_seq");
        long reindexTxId;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(selectNextValue);
            if (rs.next()) {
                reindexTxId = rs.getLong(1);
            } else {
                // not gonna happen
                throw new IllegalStateException("No value from sequence");
            }
        } catch (SQLException x) {
            logger.log(Level.SEVERE, selectNextValue, x);
            throw x;
        }

        // TODO when we put the DELETED flag into logical_resources we can filter out
        // here instead of failing the read later
        // Mark the next batch of resources we want to process by
        // updating the REINDEX_TXID field with the new value we just acquired
        // To avoid contention, we need different threads to mark (update) different
        // sets of resources. This is because the lock from the update persists for
        // the entire transaction, which could be a few seconds. Although it's probably
        // feasible to to in a (complex) query, making this a couple of steps is a
        // lot simpler.
        int rowsMarked = 0;
        List<Long> logicalResourceIds = new ArrayList<>();
        do {
            rowsMarked = 0;
            logicalResourceIds.clear();
            
            // How many rows in total should we fetch.
            int fetchCount = 100 * resourceCount;
            final String FETCH = ""
                    + "   SELECT lr.logical_resource_id "
                    + "     FROM logical_resources lr "
                    + "    WHERE reindex_tstamp < ? "
                    + "       OR reindex_tstamp IS NULL "
                    + " ORDER BY reindex_tstamp, "
                    + "          logical_resource_id "
                    + " FETCH FIRST ? ROWS ONLY";
            try (PreparedStatement stmt = connection.prepareStatement(FETCH)) {
                stmt.setTimestamp(1, Timestamp.from(reindexTstamp));
                stmt.setInt(2, fetchCount);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    logicalResourceIds.add(rs.getLong(1));
                }
            }
            
            // pick the random start point in the logicalResourceIds list. This spreads out locking
            // and reduces contention
            if (logicalResourceIds.size() > 0) {
                int blockCount = 1 + (logicalResourceIds.size()-1) / resourceCount;
                int blockNbr = random.nextInt(blockCount);
                int fromIndex = blockNbr * resourceCount;
                int toIndex = Math.min(fromIndex + resourceCount, logicalResourceIds.size());
                
                List<Long> block = logicalResourceIds.subList(fromIndex, toIndex);
    
                // Build a query to mark the randomly selected block of resource ids with our
                // txid value. Note that if another thread has done the same for this block,
                // this thread will block, and then not mark anything in this pass, hence 
                // the do...while to repeat the select and get a new list of ids
                if (block.size() > 0) {
                    boolean first = true;
                    final StringBuilder mark = new StringBuilder();
                    mark.append("   UPDATE logical_resources  ");
                    mark.append("      SET reindex_txid = ?,  ");
                    mark.append("          reindex_tstamp = ? ");
                    mark.append("    WHERE (reindex_tstamp < ? ");
                    mark.append("       OR reindex_tstamp IS NULL) ");
                    mark.append("      AND logical_resource_id IN (");
                    for (Long resourceId: block) {
                        if (first) {
                            first = false;
                        } else {
                            mark.append(",");
                        }
                        mark.append(resourceId);
                    }
                    mark.append(")");
                    
                    try (PreparedStatement stmt = connection.prepareStatement(mark.toString())) {
                        stmt.setLong(1, reindexTxId);
                        stmt.setTimestamp(2, Timestamp.from(reindexTstamp));
                        stmt.setTimestamp(3, Timestamp.from(reindexTstamp));
                        rowsMarked = stmt.executeUpdate();
                    }
                }
            }
        } while (rowsMarked == 0 && logicalResourceIds.size() > 0);
 
        if (logicalResourceIds.size() > 0) {
            // Now grab what we just marked. There's a supporting index, 
            // so this will be quick. Order doesn't matter because the
            // logical resource record is already locked thanks to the previous
            // update statement
            final String SELECT = ""
                    + "  SELECT rt.resource_type, lr.logical_id, lr.logical_resource_id "
                    + "    FROM logical_resources lr, "
                    + "         resource_types rt "
                    + "   WHERE rt.resource_type_id = lr.resource_type_id "
                    + "     AND lr.reindex_txid = ?";
            
            try (PreparedStatement ps = connection.prepareStatement(SELECT)) {
                ps.setLong(1, reindexTxId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    result.add(new ResourceIndexRecord(rs.getString(1), rs.getString(2), rs.getLong(3)));
                }
            }
        }
        return result;
    }

    /**
     * Reindex the resource by deleting existing parameters and replacing them with those passed in
     * @param tablePrefix
     * @param parameters
     * @param p_logical_id
     * @param p_payload
     * @param p_last_updated
     * @param p_is_deleted
     * @param p_source_key
     * @param p_version
     *
     * @return the resource_id for the entry we created
     * @throws Exception
     */
    public void updateParameters(String tablePrefix, List<ExtractedParameterValue> parameters, String logicalId, long logicalResourceId) throws Exception {

        final String METHODNAME = "updateParameters() for " + tablePrefix + "/" + logicalId;
        logger.entering(CLASSNAME, METHODNAME);
        
        // no need to close
        Connection connection = getConnection();

        // existing resource, so need to delete all its parameters
        // delete composites first, or else the foreign keys there restrict deletes on referenced tables
        deleteFromParameterTable(connection, tablePrefix + "_composites", logicalResourceId);
        deleteFromParameterTable(connection, tablePrefix + "_str_values", logicalResourceId);
        deleteFromParameterTable(connection, tablePrefix + "_number_values", logicalResourceId);
        deleteFromParameterTable(connection, tablePrefix + "_date_values", logicalResourceId);
        deleteFromParameterTable(connection, tablePrefix + "_latlng_values", logicalResourceId);
        deleteFromParameterTable(connection, tablePrefix + "_resource_token_refs", logicalResourceId);
        deleteFromParameterTable(connection, tablePrefix + "_quantity_values", logicalResourceId);

        if (parameters != null) {
            JDBCIdentityCache identityCache = new JDBCIdentityCacheImpl(getCache(), this, parameterDao);
            try (ParameterVisitorBatchDAO pvd = new ParameterVisitorBatchDAO(connection, null, tablePrefix, false, logicalResourceId, 100,
                identityCache, getResourceReferenceDAO())) {
                for (ExtractedParameterValue p: parameters) {
                    p.accept(pvd);
                }
            }
        }
        logger.exiting(CLASSNAME, METHODNAME);
    }

    /**
     * Delete all parameters for the given resourceId from the parameters table
     *
     * @param conn
     * @param tableName
     * @param logicalResourceId
     * @throws SQLException
     */
    protected void deleteFromParameterTable(Connection conn, String tableName, long logicalResourceId) throws SQLException {
        final String delStrValues = "DELETE FROM " + tableName + " WHERE logical_resource_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(delStrValues)) {
            // bind parameters
            stmt.setLong(1, logicalResourceId);
            stmt.executeUpdate();
        }

    }
}
