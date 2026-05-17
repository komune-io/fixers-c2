package io.komune.c2.chaincode.api.fabric

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.Assertions.assertTrue

class FabricGatewayClientValidationCategorisationTest {

    @ParameterizedTest(name = "{0} → {1}, errorClass={3}")
    @MethodSource("validationCases")
    fun `validation code maps to expected outcome class`(
        codeName: String, expectedClass: String, expectedErrorCode: String, expectedErrorClass: String,
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
                assertThat(conflict.errorClass).isEqualTo(expectedErrorClass)
            }
            "Rejected" -> {
                assertThat(outcome).isInstanceOf(TxOutcome.Rejected::class.java)
                val rejected = outcome as TxOutcome.Rejected
                assertThat(rejected.errorCode).isEqualTo(expectedErrorCode)
                assertThat(rejected.errorClass).isEqualTo(expectedErrorClass)
            }
            "Indeterminate" -> {
                assertThat(outcome).isInstanceOf(TxOutcome.Indeterminate::class.java)
                val indeterminate = outcome as TxOutcome.Indeterminate
                assertThat(indeterminate.errorCode).isEqualTo(expectedErrorCode)
                assertThat(indeterminate.errorClass).isEqualTo(expectedErrorClass)
            }
        }
    }

    companion object {
        @JvmStatic
        fun validationCases(): List<Arguments> = listOf(
            // STATE class — MVCC / phantom / duplicate / writeset conflicts
            Arguments.of("MVCC_READ_CONFLICT", "Conflict", "MVCC_READ_CONFLICT", "STATE"),
            Arguments.of("PHANTOM_READ_CONFLICT", "Conflict", "PHANTOM_READ_CONFLICT", "STATE"),
            Arguments.of("INVALID_OTHER_REASON", "Conflict", "INVALID_OTHER_REASON", "STATE"),
            Arguments.of("DUPLICATE_TXID", "Conflict", "DUPLICATE_TXID", "STATE"),
            Arguments.of("INVALID_WRITESET", "Conflict", "INVALID_WRITESET", "STATE"),
            // BUSINESS class — endorsement policy
            Arguments.of("ENDORSEMENT_POLICY_FAILURE", "Rejected", "ENDORSEMENT_POLICY_FAILURE", "BUSINESS"),
            // AUTH class
            Arguments.of("UNAUTHORISED", "Rejected", "UNAUTHORISED", "AUTH"),
            // INFRA class
            Arguments.of("TARGET_CHAIN_NOT_FOUND", "Rejected", "TARGET_CHAIN_NOT_FOUND", "INFRA"),
            // INPUT class — bad format / schema / chaincode codes
            Arguments.of("BAD_RWSET", "Rejected", "BAD_RWSET", "INPUT"),
            Arguments.of("BAD_CHANNEL_HEADER", "Rejected", "BAD_CHANNEL_HEADER", "INPUT"),
            Arguments.of("BAD_HEADER_EXTENSION", "Rejected", "BAD_HEADER_EXTENSION", "INPUT"),
            Arguments.of("INVALID_CONFIG_TRANSACTION", "Rejected", "INVALID_CONFIG_TRANSACTION", "INPUT"),
            Arguments.of("MARSHAL_TX_ERROR", "Rejected", "MARSHAL_TX_ERROR", "INPUT"),
            Arguments.of("NIL_ENVELOPE", "Rejected", "NIL_ENVELOPE", "INPUT"),
            Arguments.of("BAD_PAYLOAD", "Rejected", "BAD_PAYLOAD", "INPUT"),
            Arguments.of("EXPIRED_CHAINCODE", "Rejected", "EXPIRED_CHAINCODE", "INPUT"),
            // UNKNOWN class — defensive bucket
            Arguments.of("NOT_VALIDATED", "Indeterminate", "NOT_VALIDATED", "UNKNOWN"),
            Arguments.of("SOME_FUTURE_CODE", "Indeterminate", "SOME_FUTURE_CODE", "UNKNOWN"),
        )
    }
}
