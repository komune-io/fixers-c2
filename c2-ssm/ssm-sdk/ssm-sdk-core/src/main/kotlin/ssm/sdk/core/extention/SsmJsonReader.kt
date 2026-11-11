package ssm.sdk.core.extention

import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmName
import ssm.sdk.json.JsonUtils
import ssm.sdk.sign.FileUtils

class SsmJsonReader {

    fun readSsm(ssmName: SsmName): Ssm = readSsm("$ssmName.json", ssmName)

    fun readSsm(fileName: String, ssmName: String): Ssm {
        val ssm: Ssm = FileUtils.getReader(fileName).use { JsonUtils.toObject(it, Ssm::class.java) }
        return Ssm(ssmName, ssm.transitions)
    }
}
