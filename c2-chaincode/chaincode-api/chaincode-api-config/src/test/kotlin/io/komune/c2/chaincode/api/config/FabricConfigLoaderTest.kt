package io.komune.c2.chaincode.api.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class FabricConfigLoaderTest {

	private val configuration = C2ChaincodeConfiguration(
		defaultCcid = "sandbox/ex02",
		ccid = "sandbox/ex02,sandbox/ssm",
		user = C2ChaincodeConfiguration.UserConfig().apply {
			name = "user"
			password = "password"
			org = "bclan"
		},
		config = C2ChaincodeConfiguration.FileConfig().apply {
			file = "file:./config/fabric/config.json"
			crypto = "file:./config/fabric/"
		},
		endorsers = "peer0:bclan"
	)

	private val loader = FabricConfigLoader(configuration)

	@Test
	fun `getChannelConfig should return the config of a known channel`() {
		val channelConfig = loader.getChannelConfig("sandbox")
		assertThat(channelConfig.channelId).isEqualTo("sandbox")
		assertThat(channelConfig.chaincodeId).containsExactly("ex02", "ssm")
	}

	@Test
	fun `getChannelConfig should fail on unknown channel`() {
		assertThatThrownBy { loader.getChannelConfig("unknown") }
			.isInstanceOf(ChannelConfigNotFoundException::class.java)
			.hasMessage("unknown")
	}
}
