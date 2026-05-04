package io.komune.c2.chaincode.api.config.properties

import io.komune.c2.chaincode.api.config.utils.HasKeystore
import io.komune.c2.chaincode.api.config.utils.HasSigncerts
import io.komune.c2.chaincode.api.config.utils.HasTlsCacerts

class PeerProperties(
    val requests: String,
    val events: String,
    val serverHostname: String,
    override val tlsCacerts: String,
    override val keystore: String,
    override val signcerts: String,
) : HasTlsCacerts, HasKeystore, HasSigncerts
