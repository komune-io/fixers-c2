package io.komune.c2.chaincode.api.config.properties

data class NetworkProperties(
    val orderer: OrdererProperties,
    val organisations: Map<String, OrganisationProperties>
)
