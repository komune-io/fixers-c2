package ssm.data.dsl

import ssm.data.dsl.features.query.DataChaincodeListQueryFunction
import ssm.data.dsl.features.query.DataSsmGetQueryFunction
import ssm.data.dsl.features.query.DataSsmListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionListQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogGetQueryFunction
import ssm.data.dsl.features.query.DataSsmSessionLogListQueryFunction

/**
 * The query functions of the SSM data API.
 * - fun dataChaincodeListQueryFunction(): [DataChaincodeListQueryFunction]
 * - fun dataSsmListQueryFunction(): [DataSsmListQueryFunction]
 * - fun dataSsmGetQueryFunction(): [DataSsmGetQueryFunction]
 * - fun dataSsmSessionListQueryFunction(): [DataSsmSessionListQueryFunction]
 * - fun dataSsmSessionGetQueryFunction(): [DataSsmSessionGetQueryFunction]
 * - fun dataSsmSessionLogListQueryFunction(): [DataSsmSessionLogListQueryFunction]
 * - fun dataSsmSessionLogGetQueryFunction(): [DataSsmSessionLogGetQueryFunction]
 * @d2 model
 * @title Entrypoints
 * @parent [ssm.data.dsl.DataSsmD2]
 * @title Synthesis and global objects of the API
 */
// Kotlin only: F2Function is not exported to JS, which reaches these functions through f2's JS facade.
interface SsmApiQueryFunctions {
    fun dataChaincodeListQueryFunction(): DataChaincodeListQueryFunction
    fun dataSsmListQueryFunction(): DataSsmListQueryFunction
    fun dataSsmGetQueryFunction(): DataSsmGetQueryFunction
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun dataSsmSessionListQueryFunction(): DataSsmSessionListQueryFunction
    fun dataSsmSessionGetQueryFunction(): DataSsmSessionGetQueryFunction
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun dataSsmSessionLogListQueryFunction(): DataSsmSessionLogListQueryFunction
    // The name is the F2 bean the starter publishes, which consumers inject by name.
    @Suppress("FunctionNameMaxLength")
    fun dataSsmSessionLogGetQueryFunction(): DataSsmSessionLogGetQueryFunction
}
