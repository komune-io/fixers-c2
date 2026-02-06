package io.komune.c2.chaincode.api.fabric;

import java.util.UUID;
import org.hyperledger.fabric.sdk.User;
import org.junit.jupiter.api.Test;


import static io.komune.c2.chaincode.api.fabric.FabricChainCodeClientTest.CLIENT_CONFIG;
import static io.komune.c2.chaincode.api.fabric.FabricChainCodeClientTest.CRYPTO_CONFIG;
import static io.komune.c2.chaincode.api.fabric.FabricChainCodeClientTest.USER_NAME;
import static io.komune.c2.chaincode.api.fabric.FabricChainCodeClientTest.USER_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

class FabricUserClientTest {

    @Test
    void register() throws Exception {
        FabricUserClient client = FabricUserClient.fromConfigFile(CLIENT_CONFIG, CRYPTO_CONFIG);
        String userName = "Adrien"+ UUID.randomUUID().toString();
        String val = client.register(USER_NAME, USER_PASSWORD, "bclan", userName, "adrienpass");
        assertThat(val).isEqualTo("adrienpass");
    }

    @Test
    void enroll() throws Exception {
        FabricUserClient client = FabricUserClient.fromConfigFile(CLIENT_CONFIG, CRYPTO_CONFIG);
        User user = client.enroll(USER_NAME, USER_PASSWORD, "bclan");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(USER_NAME);
    }
}