package io.komune.c2.chaincode.api.fabric.config;

import java.io.IOException;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;


public class FabricConfigTest {

    @Test
    public void testLoadFromFile() throws IOException {
        FabricConfig config = FabricConfig.loadFromFile("configuration/config.json");
        assertThat(config.getNetwork()).isNotNull();
        assertThat(config.getNetwork().getOrderer().getServerHostname()).isEqualTo("orderer.bclan");
        assertThat(config.getNetwork().getOrderer().getUrl()).isEqualTo("grpcs://orderer.bclan:7050");
        assertThat(config.getNetwork().getOrderer().getTlsCacerts()).isEqualTo("crypto-config/ordererOrganizations/bclan/orderers/orderer.bclan/msp/tlscacerts/tlsca.bclan-cert.pem");

        assertThat(config.getNetwork().getOrganisations()).containsKeys("bclan");
        assertThat(config.getNetwork().getOrganisation("bclan").getMspid()).isEqualTo("BlockchainLANCoopMSP");
        assertThat(config.getNetwork().getOrganisation("bclan").getName()).isEqualTo("BlockchainLANCoop");

    }

}
