package ssm.chaincode.dsl

/**
 * Next to the blockchain, there may be a CouchDB database duplicating some data
 * within the chain for query optimization purpose.
 * As it is a more classic database, queries take much less time to complete
 * but the data lose the benefits of being in a blockchain.
 * @D2 page
 * @title SSM-CHAINCODE/General
 */
interface SsmChaincodeD2

/**
 * Test description config.
 * @d2 page
 * @title SSM-CHAINCODE/Models
 */
interface SsmChaincodeD2Model

/**
 * Test description config.
 * @d2 page
 * @parent [SsmChaincodeD2]
 * @title SSM-CHAINCODE/Query functions
 */
interface SsmChaincodeD2Query

