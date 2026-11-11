package ssm.data.dsl.features.query

import kotlin.js.JsExport
import kotlin.js.JsName
import ssm.chaincode.dsl.model.uri.SsmUriDTO

@JsExport
@JsName("DataQueryDTO")
interface DataQueryDTO {
    /**
     * Uri information to access a ssm.
     * @example "ssm:channel:chaincode:ssmName"
     */
    val ssmUri: SsmUriDTO
}
