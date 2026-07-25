package ssm.chaincode.dsl.model.uri

import io.komune.c2.chaincode.dsl.ChaincodeUri
import io.komune.c2.chaincode.dsl.from
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SsmUriTest {

    @Test
    fun `parses a valid ssm uri into its parts`() {
        val uri = SsmUri("ssm:sandbox:ssm:CACertificate")
        assertThat(uri.channelId).isEqualTo("sandbox")
        assertThat(uri.chaincodeId).isEqualTo("ssm")
        assertThat(uri.ssmName).isEqualTo("CACertificate")
        assertThat(uri.ssmVersion).isEqualTo(DEFAULT_VERSION)
    }

    @Test
    fun `chaincodeUri projects the channel and chaincode part`() {
        assertThat(SsmUri("ssm:sandbox:ssm:CACertificate").chaincodeUri)
            .isEqualTo(ChaincodeUri("chaincode:sandbox:ssm"))
    }

    @Test
    fun `from ids builds the canonical uri`() {
        val uri = SsmUri.from(channelId = "ch", chaincodeId = "cc", ssmName = "Machine")
        assertThat(uri.uri).isEqualTo("ssm:ch:cc:Machine")
    }

    @Test
    fun `from chaincode uri and toSsmUri round-trip`() {
        val chaincodeUri = ChaincodeUri.from(channelId = "ch", chaincodeId = "cc")
        assertThat(SsmUri.from(chaincodeUri, "Machine")).isEqualTo(SsmUri("ssm:ch:cc:Machine"))
        assertThat(chaincodeUri.toSsmUri("Machine")).isEqualTo(SsmUri("ssm:ch:cc:Machine"))
    }

    @Test
    fun `burst and asChaincodeUri work from the DTO view`() {
        val dto: SsmUriDTO = SsmUri("ssm:ch:cc:Machine")
        assertThat(dto.burst()).isEqualTo(SsmUri("ssm:ch:cc:Machine"))
        assertThat(dto.asChaincodeUri()).isEqualTo(ChaincodeUri("chaincode:ch:cc"))
    }

    @Test
    fun `rejects a uri with the wrong number of parts or prefix`() {
        assertThatThrownBy { SsmUri("ssm:ch:cc") }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { SsmUri("ssm:ch:cc:name:extra") }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { SsmUri("chaincode:ch:cc:name") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
