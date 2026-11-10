package ssm.data.dsl.config

import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.couchdb.dsl.config.SsmCouchdbConfig

/**
 * @d2 model
 * @title SSM Configuration
 * @parent [ssm.data.dsl.DataSsmD2]
 */
data class DataSsmConfig(
    /**
     * Configuration for chunking.
     */
    val batch: SsmBatchProperties,
    /**
     * Configuration for couchdb.
     */
    val couchdb: SsmCouchdbConfig,
    /**
     *  Configuration for couchdb.
     */
    val chaincode: SsmChaincodeProperties,

    )
