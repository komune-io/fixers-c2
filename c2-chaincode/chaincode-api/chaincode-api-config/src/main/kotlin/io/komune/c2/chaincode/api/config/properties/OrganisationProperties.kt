package io.komune.c2.chaincode.api.config.properties


data class OrganisationProperties(
    val name: String,
    val mspid: String,
    val ca: CaProperties,
    val peers: Map<String, PeerProperties>
)
