package ssm.chaincode.dsl.model.uri

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.ChaincodeUriDTO
import io.komune.c2.chaincode.dsl.burst
import io.komune.c2.chaincode.dsl.from
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlinx.serialization.Serializable
import ssm.chaincode.dsl.model.ChaincodeId
import ssm.chaincode.dsl.model.ChannelId
import ssm.chaincode.dsl.model.SsmName

typealias SsmVersion = String

const val DEFAULT_VERSION = "1.0.0"

@JsExport
@JsName("SsmUriDTO")
interface SsmUriDTO {
    val uri: String
}

fun SsmUriDTO.burst() = SsmUri(uri)
fun SsmUriDTO.asChaincodeUri() = SsmUri(uri).chaincodeUri

@Serializable
@JsExport
@JsName("SsmUri")
data class SsmUri(override val uri: String): SsmUriDTO {

    private val burst = uri.split(":")

    init {
        require(burst.size == PARTS)
        require(burst.first() == PREFIX)
    }

    val channelId
        get() = burst[1]
    val chaincodeId
        get() = burst[2]
    val ssmName
        get() = burst[3]
    val ssmVersion
        get() = DEFAULT_VERSION
    val chaincodeUri
        get() = ChaincodeUri.from(channelId, chaincodeId)

    companion object {
        const val PARTS = 4
        const val PREFIX = "ssm"
    }
}

fun ChaincodeUriDTO.toSsmUri(ssmName: SsmName): SsmUri =
    SsmUri.from(channelId = burst().channelId, chaincodeId = burst().chaincodeId, ssmName = ssmName)

fun SsmUri.Companion.from(
    chaincodeUri: ChaincodeUri,
    ssmName: SsmName,
) = SsmUri("${PREFIX}:${chaincodeUri.channelId}:${chaincodeUri.chaincodeId}:$ssmName")

fun SsmUri.Companion.from(
         channelId: ChannelId,
         chaincodeId: ChaincodeId,
         ssmName: SsmName,
) = SsmUri("${PREFIX}:$channelId:$chaincodeId:$ssmName")
