package io.komune.c2.chaincode.api.fabric

import org.hyperledger.fabric.protos.peer.TxValidationCode
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class TxValidationCodeMapperTest {

    @Test
    fun `VALID maps to Committed`() {
        val outcome = TxValidationCodeMapper.toOutcome(
            commandId = "c1",
            transactionId = "tx1",
            blockNumber = 42L,
            code = TxValidationCode.VALID,
        )
        assertThat(outcome).isInstanceOf(TxOutcome.Committed::class.java)
        assertThat((outcome as TxOutcome.Committed).blockNumber).isEqualTo(42L)
    }

    @Test
    fun `MVCC_READ_CONFLICT maps to Conflict`() {
        val outcome = TxValidationCodeMapper.toOutcome("c1", "tx1", 42L, TxValidationCode.MVCC_READ_CONFLICT)
        assertThat(outcome).isInstanceOf(TxOutcome.Conflict::class.java)
        assertThat((outcome as TxOutcome.Conflict).errorCode).isEqualTo("MVCC_READ_CONFLICT")
    }

    @Test
    fun `ENDORSEMENT_POLICY_FAILURE maps to Conflict`() {
        val outcome = TxValidationCodeMapper.toOutcome("c1", "tx1", 42L, TxValidationCode.ENDORSEMENT_POLICY_FAILURE)
        assertThat(outcome).isInstanceOf(TxOutcome.Conflict::class.java)
    }

    @Test
    fun `INVALID_OTHER_REASON maps to Conflict with code`() {
        val outcome = TxValidationCodeMapper.toOutcome("c1", "tx1", 42L, TxValidationCode.INVALID_OTHER_REASON)
        assertThat(outcome).isInstanceOf(TxOutcome.Conflict::class.java)
        assertThat((outcome as TxOutcome.Conflict).errorCode).isEqualTo("INVALID_OTHER_REASON")
    }
}
