package io.komune.c2.chaincode.api.fabric

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.Assertions.assertTrue

class FabricGatewayClientValidationCategorisationTest {

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("validationCases")
    fun `validation code maps to expected outcome class`(
        codeName: String, expectedClass: String, expectedErrorCode: String,
    ) {
        val outcome = TxValidationCodeMapper.toOutcome(
            commandId = "cmd-1",
            statusCodeName = codeName,
            transactionId = "tx-1",
            blockNumber = 42L,
        )
        when (expectedClass) {
            "Conflict" -> {
                assertThat(outcome).isInstanceOf(TxOutcome.Conflict::class.java)
                val conflict = outcome as TxOutcome.Conflict
                assertThat(conflict.errorCode).isEqualTo(expectedErrorCode)
                assertThat(conflict.transactionId).isEqualTo("tx-1")
                assertThat(conflict.blockNumber).isEqualTo(42L)
            }
            "Rejected" -> {
                assertThat(outcome).isInstanceOf(TxOutcome.Rejected::class.java)
                val rejected = outcome as TxOutcome.Rejected
                assertThat(rejected.errorCode).isEqualTo(expectedErrorCode)
            }
            "Indeterminate" -> {
                assertThat(outcome).isInstanceOf(TxOutcome.Indeterminate::class.java)
                val indeterminate = outcome as TxOutcome.Indeterminate
                assertThat(indeterminate.errorCode).isEqualTo(expectedErrorCode)
            }
        }
    }

    companion object {
        @JvmStatic
        fun validationCases(): List<Arguments> = listOf(
            // STATE — MVCC / phantom / duplicate / writeset conflicts
            Arguments.of("MVCC_READ_CONFLICT", "Conflict", "MVCC_READ_CONFLICT"),
            Arguments.of("PHANTOM_READ_CONFLICT", "Conflict", "PHANTOM_READ_CONFLICT"),
            Arguments.of("INVALID_OTHER_REASON", "Conflict", "INVALID_OTHER_REASON"),
            Arguments.of("DUPLICATE_TXID", "Conflict", "DUPLICATE_TXID"),
            Arguments.of("INVALID_WRITESET", "Conflict", "INVALID_WRITESET"),
            // BUSINESS — endorsement policy
            Arguments.of("ENDORSEMENT_POLICY_FAILURE", "Rejected", "ENDORSEMENT_POLICY_FAILURE"),
            // AUTH
            Arguments.of("UNAUTHORISED", "Rejected", "UNAUTHORISED"),
            // INFRA
            Arguments.of("TARGET_CHAIN_NOT_FOUND", "Rejected", "TARGET_CHAIN_NOT_FOUND"),
            // INPUT — bad format / schema / chaincode codes
            Arguments.of("BAD_RWSET", "Rejected", "BAD_RWSET"),
            Arguments.of("BAD_CHANNEL_HEADER", "Rejected", "BAD_CHANNEL_HEADER"),
            Arguments.of("BAD_HEADER_EXTENSION", "Rejected", "BAD_HEADER_EXTENSION"),
            Arguments.of("INVALID_CONFIG_TRANSACTION", "Rejected", "INVALID_CONFIG_TRANSACTION"),
            Arguments.of("MARSHAL_TX_ERROR", "Rejected", "MARSHAL_TX_ERROR"),
            Arguments.of("NIL_ENVELOPE", "Rejected", "NIL_ENVELOPE"),
            Arguments.of("BAD_PAYLOAD", "Rejected", "BAD_PAYLOAD"),
            Arguments.of("EXPIRED_CHAINCODE", "Rejected", "EXPIRED_CHAINCODE"),
            // UNKNOWN — defensive bucket
            Arguments.of("NOT_VALIDATED", "Indeterminate", "NOT_VALIDATED"),
            Arguments.of("SOME_FUTURE_CODE", "Indeterminate", "SOME_FUTURE_CODE"),
        )
    }
}
