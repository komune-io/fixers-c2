package io.komune.c2.chaincode.api.fabric.factory;

import io.komune.c2.chaincode.api.fabric.config.FabricConfig;
import io.komune.c2.chaincode.api.fabric.config.OrganisationConfig;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.helper.Config;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricClientFactory {

    private Logger logger = LoggerFactory.getLogger(FabricClientFactory.class);

    private String cryptoConfigBase;
    private FabricConfig fabricConfig;

    public FabricClientFactory(FabricConfig fabricConfig, String cryptoConfigBase) {
        this.fabricConfig = fabricConfig;
        this.cryptoConfigBase = cryptoConfigBase;
    }

    public static FabricClientFactory factory(FabricConfig fabricConfig, String cryptoConfigBase) {
        return new FabricClientFactory(fabricConfig, cryptoConfigBase);
    }

    public HFCAClient getHfCaClient(String orgName) throws Exception {
        OrganisationConfig config = fabricConfig.getNetwork().getOrganisation(orgName);
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient caClient = HFCAClient.createNewInstance(config.getCa().getUrl(), config.getCa().getPeerTlsProperties(cryptoConfigBase));
        caClient.setCryptoSuite(cryptoSuite);
        return caClient;
    }

    public HFClient getHfClient(User admin) throws Exception {
        HFClient client = getHfClient();
        client.setUserContext(admin);
        return client;
    }

    private HFClient getHfClient() throws Exception {
        int clientThreadExecutorCorePoolSize = Config.getConfig().getClientThreadExecutorCorePoolSize();
        logger.info("/////////////////////////////////");
        logger.info("/////////////////////////////////");
        logger.info(""+clientThreadExecutorCorePoolSize);
        logger.info("/////////////////////////////////");
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }

}
