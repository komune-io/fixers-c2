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

    fun getEndorsers(): List<Endorser> = Endorser.fromListPair(endorsers)

    fun getCcids(): Array<String> = ccid.split(",").toTypedArray()

    fun getChannelChaincodes(): Map<ChannelId, ChannelConfig> {
        val config = requireNotNull(config) { "Missing coop.config in application.yml" }
        return ChannelConfig.fromConfig(
            lines = getCcids(),
            user = user,
            config = config,
            endorsers = getEndorsers()
        )
    }

    fun getChannelChaincodePair(channelId: ChannelId?, chainCodeId: String?): ChannelChaincodePair {
        val channelAndChaincodeFromConfig = ChannelChaincodePair.fromConfig(defaultCcid)

        val actualChannelChaincodePair =
            ChannelChaincodePair(
                channelId = channelId ?: channelAndChaincodeFromConfig.channelId,
                chainCodeId = chainCodeId ?: channelAndChaincodeFromConfig.chainCodeId
            )

        val givenCcid = "${actualChannelChaincodePair.channelId}$CCID_SEPARATOR" +
            actualChannelChaincodePair.chainCodeId
        if (!ccid.contains(givenCcid)) {
            throw InvokeException("Invalid $givenCcid")
        }

        logger.debug(
            "chaincode found " +
                    "from [${channelId ?: DEFAULT_LABEL}:${chainCodeId ?: DEFAULT_LABEL}] " +
                    "is ${actualChannelChaincodePair.channelId}:${actualChannelChaincodePair.chainCodeId}"
        )
        return actualChannelChaincodePair
    }

    fun getBatch() = batch?.let { Batch(size = it.size, concurrency = it.concurrency) } ?: Batch()

    data class UserConfig(
        val name: String,
        val password: String,
        val org: String
    ) {
        override fun toString(): String = "UserConfig(name=$name, org=$org)"
    }

    data class FileConfig(
        val file: String,
        val crypto: String
    )

    companion object {
        const val CCID_SEPARATOR = "/"
        private const val DEFAULT_LABEL = "default"
    }
}

open class BatchProperties {
    var size: Int = BATCH_DEFAULT_SIZE
    var concurrency: Int = BATCH_DEFAULT_CONCURRENCY
}
