package ssm.data.dsl.features.query

import ssm.chaincode.dsl.model.uri.SsmUri
import ssm.data.dsl.model.DataSsm

actual interface DataSsmGetQueryDTO : DataQueryDTO {
	actual override val ssm: SsmUri
	actual override val bearerToken: String?
}

actual interface DataSsmGetQueryResultDTO {
	actual val ssm: DataSsm?
}
