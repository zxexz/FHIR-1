(window.webpackJsonp=window.webpackJsonp||[]).push([[9],{370:function(e,t,n){"use strict";n.r(t),n.d(t,"_frontmatter",function(){return i}),n.d(t,"default",function(){return l});n(11),n(6),n(7),n(5),n(10),n(0);var r=n(107),a=n(394);n(1);function s(){return(s=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var n=arguments[t];for(var r in n)Object.prototype.hasOwnProperty.call(n,r)&&(e[r]=n[r])}return e}).apply(this,arguments)}var i={},o={_frontmatter:i},c=a.a;function l(e){var t=e.components,n=function(e,t){if(null==e)return{};var n,r,a={},s=Object.keys(e);for(r=0;r<s.length;r++)n=s[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,["components"]);return Object(r.b)(c,s({},o,n,{components:t,mdxType:"MDXLayout"}),Object(r.b)("h2",null,"Overview"),Object(r.b)("p",null,"The IBM FHIR Server ships with a JDBC persistence layer that works with IBM Db2 and Apache Derby.\nHowever, based on the modular design, its possible to add support for other relational databases and/or\nplug in any other persistence layer."),Object(r.b)("h3",null,"Interfaces"),Object(r.b)("p",null,"Persistence layer interfaces are defined in the ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence")," project."),Object(r.b)("ul",null,Object(r.b)("li",{parentName:"ul"},Object(r.b)("a",s({parentName:"li"},{href:"https://github.com/IBM/FHIR/blob/master/fhir-persistence/src/main/java/com/ibm/fhir/persistence/FHIRPersistence.java"}),"FHIRPersistence")," defines the contract between the REST layer and the persistence layer."),Object(r.b)("li",{parentName:"ul"},Object(r.b)("a",s({parentName:"li"},{href:"https://github.com/IBM/FHIR/blob/master/fhir-persistence/src/main/java/com/ibm/fhir/persistence/FHIRPersistenceFactory.java"}),"FHIRPersistenceFactory")," is the interface for providing instances of FHIRPersistence to the server.")),Object(r.b)("h3",null,"Config"),Object(r.b)("p",null,"Which persistence layer is used by the server is determined by the ",Object(r.b)("inlineCode",{parentName:"p"},"/fhirServer/persistence/factoryClassname")," property in ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-server-config.json"),"."),Object(r.b)("p",null,"When the default ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.persistence.jdbc.FHIRPersistenceJDBCFactory")," is used, the returned FHIRPersistenceJDBCImpl instance will look up a corresponding datasource name using the value of the ",Object(r.b)("inlineCode",{parentName:"p"},"fhirServer/persistence/jdbc/dataSourceJndiName")," property in the tenant-specific configuration (",Object(r.b)("inlineCode",{parentName:"p"},"jdbc/fhirProxyDataSource")," by default) and establish the connection."),Object(r.b)("h2",null,"Adding support for another relational database"),Object(r.b)("p",null,"Adding a new relational database type is not for the faint of heart, but the IBM FHIR Server team is here to help!\nTo add support for an alternative relational database, there are multiple projects to consider:"),Object(r.b)("ol",null,Object(r.b)("li",{parentName:"ol"},Object(r.b)("inlineCode",{parentName:"li"},"fhir-persistence-proxy")),Object(r.b)("li",{parentName:"ol"},Object(r.b)("inlineCode",{parentName:"li"},"fhir-database-utils")),Object(r.b)("li",{parentName:"ol"},Object(r.b)("inlineCode",{parentName:"li"},"fhir-persistence-schema")),Object(r.b)("li",{parentName:"ol"},Object(r.b)("inlineCode",{parentName:"li"},"fhir-persistence-jdbc"))),Object(r.b)("p",null,"The ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence-proxy")," project provides the default DataSource used by the IBM FHIR Server, but its just a wrapper for the XADataSources provided by the packaged JDBC drivers. To extend this class for a new database type, extend the ",Object(r.b)("inlineCode",{parentName:"p"},"datasourceTypeMapping")," with a mapping from your own type name (used in ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-server-config"),") to a classname from your driver that implements the ",Object(r.b)("inlineCode",{parentName:"p"},"javax.sql.XADataSource")," interface). Note that the proxy jar and your driver must be packaged in the ",Object(r.b)("inlineCode",{parentName:"p"},"fhirSharedLib")," library defined in the server’s ",Object(r.b)("inlineCode",{parentName:"p"},"server.xml")," in order to use liberty-managed transactions (e.g. for performing a transaction that spans multiple datasources)."),Object(r.b)("p",null,"The ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-database-utils")," project provides generic utilities for defining a PhysicalDataModel and applying it to a target database via the IDatabaseAdapter and IVersionHistoryService interfaces. Check out the ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.database.utils.db2")," and ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.database.utils.derby")," packages to understand how you might extend the framework with support for a new database type."),Object(r.b)("p",null,"The ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence-schema")," project is used to programmatically construct DDL statements and execute them against a target database. This project uses the generic utilities in ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence-utils")," to deploy both an admin schema (used for tenant provisioning and schema versioning) and an application schema for the FHIR data.\nPresently, this project is written for use with a Db2 database, but it should be possible to either:"),Object(r.b)("p",null,"A. Print the DDL and manually tweak it for your desired database; or\nB. Add some kind of configuration to control which IDatabaseAdapter and IDatabaseTranslator are used."),Object(r.b)("p",null,"Note that the Db2 implementation makes use of stored procedures whereas the derby implementation is pure JDBC."),Object(r.b)("p",null,"Finally, the ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence-jdbc")," project provides the default implementation of the ",Object(r.b)("inlineCode",{parentName:"p"},"FHIRPersistence")," interface.\nThe project makes heavy use of Data Access Objects (DAO) and Data Transfer Objects (DTO) to abstract the details of the database. Most of the code is common across database types, but there is a branch in ",Object(r.b)("inlineCode",{parentName:"p"},"ResourceDAOImpl.insert")," which corresponds to the differences between the IBM Db2 (stored procedure) and Apache Derby (pure JDBC) implementations."),Object(r.b)("h2",null,"Building your own persistence layer"),Object(r.b)("p",null,"Most FHIR projects are interoperability projects—the data already exists in some datastore.\nDue to performance considerations and the complexities of the FHIR API (especially search), we generally recommend converting that data to FHIR and storing it in the FHIR server’s database. However, in some cases, it might be better to configure the FHIR server to work directly with an existing datastore or APIs."),Object(r.b)("p",null,"If you are using Maven, add the following dependencies to your persistence layer project (replacing the version variables with your desired version):"),Object(r.b)("pre",null,Object(r.b)("code",s({parentName:"pre"},{}),"        <dependency>\n            <groupId>com.ibm.fhir</groupId>\n            <artifactId>fhir-persistence</artifactId>\n            <version>${fhir.persistence.version}</version>\n        </dependency>\n        <dependency>\n            <groupId>com.ibm.fhir</groupId>\n            <artifactId>fhir-persistence</artifactId>\n            <version>${fhir.persistence.version}</version>\n            <type>test-jar</type>\n            <scope>test</scope>\n        </dependency>\n        <dependency>\n            <groupId>com.ibm.fhir</groupId>\n            <artifactId>fhir-examples</artifactId>\n            <version>${fhir.examples.version}</version>\n            <scope>test</scope>\n        </dependency>\n")),Object(r.b)("p",null,"You might also want to set up ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence-jdbc")," to use an example."),Object(r.b)("h3",null,"Implementing the FHIRPersistence Interface"),Object(r.b)("p",null,"As described ",Object(r.b)("a",s({parentName:"p"},{href:"#interfaces"}),"above"),", implementing your own persistence layer boils down to configuring the server to use your own FHIRPersistenceFactory and returning your own implementation of FHIRPersistence. The REST layer processes HTTP requests and distills them into one or more calls to this instance. Parameters are passed via a combination of the passed FHIRPersistenceContext and the ThreadLocal FHIRRequestContext."),Object(r.b)("p",null,"Although the HL7 FHIR specification ",Object(r.b)("a",s({parentName:"p"},{href:"https://www.hl7.org/fhir/R4/http.html#versions"}),"doesn’t strictly require all servers to support versioning"),", the IBM FHIR Server is built to be version-aware. This means that all FHIRPersistence implementations should implement the ",Object(r.b)("inlineCode",{parentName:"p"},"vread")," and ",Object(r.b)("inlineCode",{parentName:"p"},"history")," interactions.\nSimilarly, the IBM FHIR Server was written for read/write datastores and so ",Object(r.b)("inlineCode",{parentName:"p"},"create")," and ",Object(r.b)("inlineCode",{parentName:"p"},"update")," should be supported as well.\nIf you have a use case for a read-only or non-version-aware server, please contact us and consider contributing the necessary modifications to the server to make this supported."),Object(r.b)("p",null,"The IBM FHIR Server does support persistence implementations which do not support ",Object(r.b)("inlineCode",{parentName:"p"},"delete")," or transactions (e.g. for transaction bundles), so please implement ",Object(r.b)("inlineCode",{parentName:"p"},"FHIRPersistence.isTransactional()")," and ",Object(r.b)("inlineCode",{parentName:"p"},"isDeleteSupported()")," accordingly."),Object(r.b)("h4",null,"Create"),Object(r.b)("p",null,"Create requests include a FHIRPersistenceContext and an already-validated instance of the resource to create. FHIRPersistence implementations are responsible for adding the ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.id"),", ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.meta.lastUpdate"),", and ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.meta.versionId")," elements to the resource before storing it. Implementations must return a SingleResourceResult with the result of the interaction."),Object(r.b)("p",null,"If ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult.success")," is set to true, the ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult.resource")," should be a copy of the saved resource (with the added fields included)."),Object(r.b)("p",null,"If ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult.success")," is set to false, the ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult.outcome")," should be an OperationOutcome with a list of one or more issues that prevented the success of the operation."),Object(r.b)("h4",null,"Read"),Object(r.b)("p",null,"Read requests include a FHIRPersistenceContext, a Class object for the resource type being read, and the logical id of the resource to read.\nImplementations should check the FHIRSearchContext of the FHIRPersistenceContext to determine whether the caller would like the full resource back, the resource text or data, or just a summary (see ",Object(r.b)("inlineCode",{parentName:"p"},"FHIRSearchContext.getSummaryParameter()"),")."),Object(r.b)("p",null,"For successful requests, ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult.success")," must be set to true and ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult.resource")," must include a ","[potentially filtered]"," resource of the type passed in the request."),Object(r.b)("p",null,"For unsuccessful requests, the implementation should return an appropriate Exception:"),Object(r.b)("ul",null,Object(r.b)("li",{parentName:"ul"},"FHIRPersistenceResourceNotFoundException if the resource with this id could not be found for the passed resource type"),Object(r.b)("li",{parentName:"ul"},"FHIRPersistenceResourceDeletedException if the latest version of this resource is marked as deleted")),Object(r.b)("p",null,"For all other errors, the implementation should return a ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult")," with a success status of false and a non-null outcome with one or more issues to indicate the failure."),Object(r.b)("p",null,"Note: we plan to deprecate these exceptions and use only ",Object(r.b)("inlineCode",{parentName:"p"},"SingleResourceResult")," as part of ",Object(r.b)("a",s({parentName:"p"},{href:"https://github.com/IBM/FHIR/issues/194"}),"https://github.com/IBM/FHIR/issues/194"),"."),Object(r.b)("h4",null,"Version read"),Object(r.b)("p",null,"Version read requests work just like read requests except that the caller passes a version identifier and the persistence implementation must return that specific version of the resource."),Object(r.b)("h4",null,"Update"),Object(r.b)("p",null,"Update requests include a FHIRPersistenceContext, a resource logical id, and an updated version of the resource to save. FHIRPersistence implementations must set the ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.meta.lastUpdate")," and ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.meta.versionId")," elements before storing it. Typically, implementations will set the version of the updated resource based on the previous version of the resource which can be found in the FHIRPersistenceEvent (",Object(r.b)("inlineCode",{parentName:"p"},"FHIRPersistenceEvent.getPrevFhirResource()"),") of the FHIRPersistenceContext."),Object(r.b)("p",null,"Note: at the REST layer, an update request will first invoke read and then invoke update. Similarly, PATCH requests are converted to normal updates before reaching the persistence layer’s update implementation."),Object(r.b)("p",null,"FHIRPersistence implementations SHOULD use the value of the ",Object(r.b)("inlineCode",{parentName:"p"},"fhirServer/persistence/common/updateCreateEnabled")," property to determine whether they should allow an update to a resource that doesn’t exist yet."),Object(r.b)("h4",null,"Delete"),Object(r.b)("p",null,"Delete requests include a FHIRPersistenceContext, a Class object for the resource type being deleted, and the logical id of the resource to delete.\nFHIRPersistence implementations are expected to be version-aware and therefore must perform a “soft” delete, handling the delete like an update by setting the ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.meta.lastUpdate")," and ",Object(r.b)("inlineCode",{parentName:"p"},"Resource.meta.versionId")," elements along with some marker flag to indicate that this resource has been deleted at this version."),Object(r.b)("p",null,"For implementations that do not implement delete, FHIRPersistence includes a default implementation which throws a FHIRPersistenceNotSupportedException."),Object(r.b)("h4",null,"History"),Object(r.b)("p",null,"The IBM FHIR Server currently only supports history at the resource instance level. History requests include a FHIRPersistenceContext with an embedded FHIRHistoryContext, a Class object for the resource type being requested, and the logical id of the resource for which to show the history. Implementations should also check the FHIRSearchContext of the FHIRPersistenceContext to determine whether the caller would like the full resources back, the resource text or data, or just a summary (see ",Object(r.b)("inlineCode",{parentName:"p"},"FHIRSearchContext.getSummaryParameter()"),")."),Object(r.b)("p",null,"FHIRHistoryContext extends FHIRPagingContext and provides the requested page size and page number to return.\nSimilarly, FHIRPersistence implementations should check and honor the the ",Object(r.b)("inlineCode",{parentName:"p"},"since")," attribute (when valued)."),Object(r.b)("p",null,"In addition to setting the MultiResourceResult success indicator and the resource version instances for the requested page, FHIRPersistence implementations must set the total number of versions for the requested resource (",Object(r.b)("inlineCode",{parentName:"p"},"FHIRPagingContext.setTotalCount(int)"),") and a map of deleted resource versions (",Object(r.b)("inlineCode",{parentName:"p"},"FHIRHistoryContext.setDeletedResources()"),") for the REST layer to properly construct the response bundle and accurately reflect which versions are deletes (rather than updates)."),Object(r.b)("h4",null,"Search"),Object(r.b)("p",null,"The ",Object(r.b)("a",s({parentName:"p"},{href:"https://www.hl7.org/fhir/R4/search.html"}),"FHIR Search specification")," is sprawling and difficult to implement in its entirety. At the persistence layer, search requests will include a FHIRPersistenceContext with an embedded FHIRSearchContext and a Class object to indicate the resource type(s) to search on.\nA Class of ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.model.type.Resource")," is passed for searches performed at the “whole-system” level."),Object(r.b)("p",null,"The query parameters passed in the HTTP request are parsed at the REST layer and passed to the persistence layer in the form of a FHIRSearchContext.\nThe FHIRSearchContext separates “return” parameters (like ",Object(r.b)("inlineCode",{parentName:"p"},"_include"),", ",Object(r.b)("inlineCode",{parentName:"p"},"_revinclude"),", ",Object(r.b)("inlineCode",{parentName:"p"},"_sort"),", etc.) from search parameters and makes them available through dedicated getters.\nEach search parameter is parsed into a QueryParameter and a QueryParameterValue. ",Object(r.b)("a",s({parentName:"p"},{href:"https://www.hl7.org/fhir/R4/compartmentdefinition.html"}),"Compartment")," searches are converted into ",Object(r.b)("a",s({parentName:"p"},{href:"https://www.hl7.org/fhir/R4/search.html#chaining"}),"chained parameters"),", which are represented through nested QueryParameter objects within the outermost QueryParameter.\nCheck ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.search.util.SearchUtil.parseQueryParameters()")," for more information."),Object(r.b)("p",null,"FHIRSearchContext extends FHIRPagingContext and provides the requested page size and page number to return.\nFHIRPersistence implementations are responsible for setting the total number of search results (",Object(r.b)("inlineCode",{parentName:"p"},"FHIRPagingContext.setTotalCount(int)"),") for the given query."),Object(r.b)("p",null,"On success, set ",Object(r.b)("inlineCode",{parentName:"p"},"MultiResourceResult.success")," to true and set ",Object(r.b)("inlineCode",{parentName:"p"},"MultiResourceResult.resource")," to the list of resources ","[or resource summaries]"," for the requested page."),Object(r.b)("p",null,"On failure, set ",Object(r.b)("inlineCode",{parentName:"p"},"MultiResourceResult.success")," to false and set ",Object(r.b)("inlineCode",{parentName:"p"},"MultiResourceResult.outcome")," to an OperationOutcome with one or more issues which indicate the failure."),Object(r.b)("h2",null,"Testing your persistence layer"),Object(r.b)("p",null,"In addition to defining the interfaces, the ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence")," project includes a set of tests that you can extend to test your implementation."),Object(r.b)("p",null,"Most of the tests defined in this project relate to search, but they also exercise the create, update, and delete interactions in the process.\nThe tests in the ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.persistence.search.test")," package are organized by search parameter type and they utilize tenant-specific search parameter definitions from the ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence/src/test/resources/config")," directory and search for fields on the generated example resources at ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-examples/src/main/resources/json/ibm/basic"),"."),Object(r.b)("p",null,"For an example of how to extend these tests, see the ",Object(r.b)("inlineCode",{parentName:"p"},"com.ibm.fhir.persistence.jdbc.search.test")," package under ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-persistence-jdbc/src/test/java"),"."),Object(r.b)("p",null,"Finally, the IBM FHIR Server contains a number of end-to-end (e2e) integration tests under the ",Object(r.b)("inlineCode",{parentName:"p"},"fhir-server-test")," project. These tests can be executed against a running server that is configured with your persistence layer to provide further confidence in your implementation."))}l.isMDXComponent=!0},392:function(e){e.exports={data:{site:{pathPrefix:"/FHIR"}}}},393:function(e){e.exports={data:{site:{siteMetadata:{repository:{baseUrl:"https://github.com/IBM/FHIR",subDirectory:"/docs",branch:"master"}}}}}}}]);
//# sourceMappingURL=component---src-pages-guides-bring-your-own-persistence-md-ae4388b00daf08806aa3.js.map