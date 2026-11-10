package ssm.chaincode.dsl.model

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("WithPrivate")
interface WithPrivate {
    val private: Map<String, String>?
}
