package ssm.api.model

import ssm.tx.dsl.model.TxChannel
import ssm.tx.dsl.model.TxSsm
import ssm.chaincode.dsl.SsmBase

fun SsmBase.toTxSsm(): TxSsm {
    return TxSsm(
        ssm = this,
        version = "Not implemented",
        channel = TxChannel("Not implemented")
    )
}