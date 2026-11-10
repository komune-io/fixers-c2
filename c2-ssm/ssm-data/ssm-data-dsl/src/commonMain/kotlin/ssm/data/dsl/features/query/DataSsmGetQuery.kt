package ssm.data.dsl.features.query

import f2.dsl.fnc.F2Function
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.data.dsl.model.DataSsm

/**
 * Retrieves a given SSM
 * @d2 function
 * @parent [ssm.data.dsl.DataSsmD2Query]
 * @order 10
 * @title Get SSM
 */
typealias DataSsmGetQueryFunction = F2Function<DataSsmGetQueryDTO, DataSsmGetQueryResultDTO>

@JsExport
@JsName("DataSsmGetQueryDTO")
interface DataSsmGetQueryDTO : DataQueryDTO {
    override val ssmUri: SsmUriDTO
}

/**
 * @d2 query
 * @parent [DataSsmGetQueryFunction]
 * @title Get SSM: Parameters
 */
@Serializable
@JsExport
@JsName("DataSsmGetQuery")
class DataSsmGetQuery(
    override val ssmUri: SsmUri,
) : DataSsmGetQueryDTO

@JsExport
@JsName("DataSsmGetQueryResultDTO")
interface DataSsmGetQueryResultDTO {
    /**
     * The retrieved SSM if it exists
     */
    val item: DataSsm?
}

/**
 * @d2 event
 * @parent [DataSsmGetQueryFunction]
 * @title Get SSM: Result
 */
@Serializable
@JsExport
@JsName("DataSsmGetQueryResult")
class DataSsmGetQueryResult(
    override val item: DataSsm?,
) : DataSsmGetQueryResultDTO
