package io.komune.c2.chaincode.dsl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ChaincodeUriTest {

    @Test
    fun `parses a valid chaincode uri into channel and chaincode ids`() {
        val uri = ChaincodeUri("chaincode:sandbox:ssm")
        assertThat(uri.channelId).isEqualTo("sandbox")
        assertThat(uri.chaincodeId).isEqualTo("ssm")
    }

    @Test
    fun `from builds the canonical uri and round-trips`() {
        val uri = ChaincodeUri.from(channelId = "sandbox", chaincodeId = "ssm")
        assertThat(uri.uri).isEqualTo("chaincode:sandbox:ssm")
        assertThat(uri.channelId).isEqualTo("sandbox")
        assertThat(uri.chaincodeId).isEqualTo("ssm")
    }

    @Test
    fun `burst rebuilds a typed uri from the DTO view`() {
        val dto: ChaincodeUriDTO = ChaincodeUri("chaincode:ch:cc")
        assertThat(dto.burst()).isEqualTo(ChaincodeUri("chaincode:ch:cc"))
    }

    @Test
    fun `rejects a uri with the wrong number of parts`() {
        assertThatThrownBy { ChaincodeUri("chaincode:sandbox") }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { ChaincodeUri("chaincode:sandbox:ssm:extra") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `rejects a uri with the wrong prefix`() {
        assertThatThrownBy { ChaincodeUri("ssm:sandbox:ssm") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
