package ssm.data.dsl.features.query

import ssm.chaincode.dsl.model.uri.SsmUri

@JsExport
@JsName("DataQueryDTO")
actual external interface DataQueryDTO {
	actual val ssmUri: SsmUri
}