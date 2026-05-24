package io.komune.c2.chaincode.api.config

import f2.dsl.fnc.operators.BATCH_DEFAULT_CONCURRENCY
import f2.dsl.fnc.operators.BATCH_DEFAULT_SIZE
import f2.dsl.fnc.operators.Batch
import io.komune.c2.chaincode.dsl.ChannelId
import io.komune.c2.chaincode.dsl.Endorser
import io.komune.c2.chaincode.dsl.invoke.InvokeException
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("coop")
class C2ChaincodeConfiguration(
	var defaultCcid: String,
	var ccid: String,
	var user: UserConfig,
	var config: FileConfig? = null,
	var endorsers: String,
	var batch: BatchProperties? = null
) {

	private val logger = LoggerFactory.getLogger(C2ChaincodeConfiguration::class.java)

	companion object {
		const val CCID_SEPARATOR = "/"
	}

	fun getEndorsers(): List<Endorser> {
		return Endorser.fromListPair(endorsers)
	}

	fun getCcids(): Array<String> = ccid.split(",").toTypedArray()

	fun getChannelChaincodes(): Map<ChannelId, ChannelConfig> {
		val user = requireNotNull(user) { "Bad user[${user}] in application.yml" }
		val config = requireNotNull(config) { "Bad config[${config}] in application.yml" }
		return ChannelConfig.fromConfig(getCcids(), user, config, getEndorsers())
	}

	fun getChannelChaincodePair(channelId: ChannelId?, chainCodeId: String?): ChannelChaincodePair {
		val channelAndChaincodeFromConfig = ChannelChaincodePair.fromConfig(defaultCcid)

		val actualChannelChaincodePair =
			ChannelChaincodePair(
				channelId = channelId ?: channelAndChaincodeFromConfig.channelId,
				chainCodeId = chainCodeId ?: channelAndChaincodeFromConfig.chainCodeId
			)

		val givenCcid = "${actualChannelChaincodePair.channelId}$CCID_SEPARATOR${actualChannelChaincodePair.chainCodeId}"
		if (!ccid.contains(givenCcid)) {
			throw InvokeException("Invalid $givenCcid")
		}

		logger.debug(
			"chaincode found " +
					"from [$channelId:$chainCodeId] " +
					"is ${actualChannelChaincodePair.channelId}:${actualChannelChaincodePair.chainCodeId}"
		)
		return actualChannelChaincodePair
	}

	fun getBatch() = batch?.let { Batch(it.size, it.concurrency) } ?: Batch()

	class UserConfig {
		lateinit var name: String
		lateinit var password: String
		lateinit var org: String
	}

	class FileConfig {
		lateinit var file: String
		lateinit var crypto: String
	}

}

open class BatchProperties {
	var size: Int = BATCH_DEFAULT_SIZE
	var concurrency: Int = BATCH_DEFAULT_CONCURRENCY
}
