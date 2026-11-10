package ssm.sdk.core.extention

import ssm.chaincode.dsl.model.Ssm
import ssm.chaincode.dsl.model.SsmName
import ssm.sdk.json.JsonUtils
import ssm.sdk.sign.FileUtils

class SsmJsonReader {

    fun readSsm(ssmName: SsmName): Ssm {
        return readSsm("$ssmName.json", ssmName)
    }

    fun readSsm(fileName: String, ssmName: String): Ssm {
        val file = FileUtils.getReader(fileName)
        val ssm: Ssm = JsonUtils.toObject(file, Ssm::class.java)
        return Ssm(ssmName, ssm.transitions)
    }
}
