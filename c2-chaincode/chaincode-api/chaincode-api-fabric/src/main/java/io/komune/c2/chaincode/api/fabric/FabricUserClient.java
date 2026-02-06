package io.komune.c2.chaincode.api.fabric;

import io.komune.c2.chaincode.api.fabric.config.FabricConfig;
import io.komune.c2.chaincode.api.fabric.factory.FabricClientFactory;
import io.komune.c2.chaincode.api.fabric.model.FabricUser;
import java.io.IOException;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

public class FabricUserClient {

    public static FabricUserClient fromConfigFile(String filename, String cryptoConfigBase) throws IOException {
        FabricConfig fabricConfig = FabricConfig.loadFromFile(filename);
        FabricClientFactory clientFactoty = FabricClientFactory.factory(fabricConfig, cryptoConfigBase);
        return new FabricUserClient(fabricConfig, clientFactoty);
    }

    private final FabricConfig fabricConfig;
    private final FabricClientFactory clientFactoty;


    public FabricUserClient(FabricConfig fabricConfig, FabricClientFactory clientFactoty) {
        this.fabricConfig = fabricConfig;
        this.clientFactoty = clientFactoty;
    }

    public String register(String user, String password, String orgName, String newUser, String newPassword) throws Exception {
        HFCAClient caClient = clientFactoty.getHfCaClient(orgName);

        User registerUser = enroll(caClient, user, password, orgName);

        RegistrationRequest request = new RegistrationRequest(newUser);
        request.setEnrollmentID(newUser);
        request.setSecret(newPassword);
        request.setType("client");

        return caClient.register(request, registerUser);
    }

    public User enroll(String user, String password, String orgName) throws Exception {
        HFCAClient caClient = clientFactoty.getHfCaClient(orgName);
        return enroll(caClient, user, password, orgName);
    }

    private User enroll(HFCAClient caClient, String user, String password, String orgName) throws Exception {
        Enrollment adminEnrollment = caClient.enroll(user, password);
        String mspid = fabricConfig.getNetwork().getOrganisation(orgName).getMspid();
        return new FabricUser(user, orgName, adminEnrollment, mspid);
    }

}
