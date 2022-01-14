package ssm.data.dsl.features.query

import ssm.chaincode.dsl.model.SessionName
import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.chaincode.dsl.model.uri.SsmUriDTO
import ssm.data.dsl.model.DataSsmSessionState

@JsExport
@JsName("DataSsmSessionLogListQueryDTO")
actual external interface DataSsmSessionLogListQueryDTO : DataQueryDTO {
	actual val sessionName: SessionName
	actual override val ssmUri: SsmUriDTO
}

@JsExport
@JsName("DataSsmSessionLogListQueryResultDTO")
actual external interface DataSsmSessionLogListQueryResultDTO {
	actual val items: List<DataSsmSessionState>
}
