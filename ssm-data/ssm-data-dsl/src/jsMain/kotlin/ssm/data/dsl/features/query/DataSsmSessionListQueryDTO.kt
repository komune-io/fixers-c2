package ssm.data.dsl.features.query

import ssm.data.dsl.model.DataSsmSessionDTO

@JsExport
@JsName("DataSsmSessionListQueryDTO")
actual external interface DataSsmSessionListQueryDTO : DataQueryDTO

@JsExport
@JsName("DataSsmSessionListQueryResultDTO")
actual external interface DataSsmSessionListQueryResultDTO {
	actual val items: List<DataSsmSessionDTO>
}