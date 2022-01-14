package ssm.data.dsl.features.query

import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.data.dsl.model.DataSsm

@JsExport
@JsName("DataSsmGetQueryDTO")
actual external interface DataSsmGetQueryDTO : DataQueryDTO {
	actual override val ssmUri: SsmUriDTO
}

@JsExport
@JsName("DataSsmGetQueryResultDTO")
actual external interface DataSsmGetQueryResultDTO {
	actual val item: DataSsm?
}
