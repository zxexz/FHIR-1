DB2 Universal Database Version 11.5, 5622-044 (c) Copyright IBM Corp. 1991, 2017
Licensed Material - Program Property of IBM
IBM DB2 Universal Database SQL and XQUERY Explain Tool

******************** SETUP SCRIPT **********************************

  SET CURRENT SCHEMA proto

Statement successful.


******************** DYNAMIC ***************************************

==================== STATEMENT ==========================================

        Isolation Level          = Cursor Stability
        Blocking                 = Block Unambiguous Cursors
        Query Optimization Class = 5

        Partition Parallel       = No
        Intra-Partition Parallel = No

        SQL Path                 = "SYSIBM", "SYSFUN", "SYSPROC", "SYSIBMADM",
                                   "DB2INST1"


Statement:

  SELECT lr.logical_id AS observation
  FROM logical_resources reference JOIN local_references map ON
          reference.logical_id ='patient1' AND reference.is_ghost ='N'
          AND reference.resource_type_id =1 AND
          map.ref_logical_resource_id =reference.logical_resource_id
          JOIN logical_resources lr ON lr.logical_resource_id =
          map.logical_resource_id


Section Code Page = 1208

Estimated Cost = 14.159569
Estimated Cardinality = 0.714286

Access Table Name = PROTO.LOGICAL_RESOURCES  ID = 4,1635
|  Index Scan:  Name = PROTO.UNQ_LOGICAL_ID  ID = 2
|  |  Regular Index (Not Clustered)
|  |  Index Columns:
|  |  |  1: LOGICAL_ID (Ascending)
|  |  |  2: RESOURCE_TYPE_ID (Ascending)
|  #Columns = 1
|  Single Record
|  Fully Qualified Unique Key
|  Skip Inserted Rows
|  Avoid Locking Committed Data
|  Currently Committed for Cursor Stability
|  Evaluate Predicates Before Locking for Key
|  #Key Columns = 2
|  |  Start Key: Inclusive Value
|  |  |  1: 'patient1'
|  |  |  2: 1
|  |  Stop Key: Inclusive Value
|  |  |  1: 'patient1'
|  |  |  2: 1
|  Data Prefetch: None
|  Index Prefetch: None
|  Lock Intents
|  |  Table: Intent Share
|  |  Row  : Next Key Share
|  Sargable Predicate(s)
|  |  #Predicates = 1
Nested Loop Join
|  Access Table Name = PROTO.LOCAL_REFERENCES  ID = 4,1636
|  |  Index Scan:  Name = PROTO.IDX_LOCAL_REFERENCES_PRL  ID = 1
|  |  |  Regular Index (Not Clustered)
|  |  |  Index Columns:
|  |  |  |  1: REF_LOGICAL_RESOURCE_ID (Ascending)
|  |  |  |  2: PARAMETER_NAME_ID (Ascending)
|  |  |  |  3: LOGICAL_RESOURCE_ID (Ascending)
|  |  #Columns = 1
|  |  Skip Inserted Rows
|  |  Avoid Locking Committed Data
|  |  Currently Committed for Cursor Stability
|  |  #Key Columns = 1
|  |  |  Start Key: Inclusive Value
|  |  |  |  1: ?
|  |  |  Stop Key: Inclusive Value
|  |  |  |  1: ?
|  |  Index-Only Access
|  |  Index Prefetch: None
|  |  Lock Intents
|  |  |  Table: Intent Share
|  |  |  Row  : Next Key Share
Nested Loop Join
|  Access Table Name = PROTO.LOGICAL_RESOURCES  ID = 4,1635
|  |  Index Scan:  Name = PROTO.PK_LOGICAL_RESOURCE  ID = 1
|  |  |  Regular Index (Not Clustered)
|  |  |  Index Columns:
|  |  |  |  1: LOGICAL_RESOURCE_ID (Ascending)
|  |  #Columns = 1
|  |  Single Record
|  |  Fully Qualified Unique Key
|  |  Skip Inserted Rows
|  |  Avoid Locking Committed Data
|  |  Currently Committed for Cursor Stability
|  |  Evaluate Predicates Before Locking for Key
|  |  #Key Columns = 1
|  |  |  Start Key: Inclusive Value
|  |  |  |  1: ?
|  |  |  Stop Key: Inclusive Value
|  |  |  |  1: ?
|  |  Data Prefetch: None
|  |  Index Prefetch: None
|  |  Lock Intents
|  |  |  Table: Intent Share
|  |  |  Row  : Next Key Share
Return Data to Application
|  #Columns = 1

End of section


Optimizer Plan:

                                                       Rows
                                                     Operator
                                                       (ID)
                                                       Cost

                                                     0.714286
                                                      RETURN
                                                       ( 1)
                                                     14.1596
                                                        |
                                                     0.714286
                                                      NLJOIN
                                                       ( 2)
                                                     14.1596
                                     /--------------/        \--------------------\
                             0.714286                                              *
                              NLJOIN                                        /-----/ \
                               ( 3)                                    1                   7
                              7.0899                                IXSCAN         Table:
                       /----/        \---------\                     ( 8)          PROTO
                   1                            *                  0.0181898       LOGICAL_RESOURCES
                 FETCH                         |                      |
                 ( 4)                          5                       7
                7.06986             Index:                    Index:
             /-/       \            PROTO                     PROTO
        1                7          IDX_LOCAL_REFERENCES_PRL  PK_LOGICAL_RESOURCE
     IXSCAN      Table:
      ( 5)       PROTO
    0.0181898    LOGICAL_RESOURCES
       |
       7
 Index:
 PROTO
 UNQ_LOGICAL_ID

