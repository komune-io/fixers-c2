package io.komune.c2.chaincode.api.fabric.factory;

import io.komune.c2.chaincode.api.fabric.config.FabricConfig;
import io.komune.c2.chaincode.api.fabric.config.OrdererConfig;
import io.komune.c2.chaincode.api.fabric.config.OrganisationConfig;
import io.komune.c2.chaincode.api.fabric.config.PeerConfig;
import io.komune.c2.chaincode.api.fabric.model.Endorser;
import java.io.IOException;
import java.util.List;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

public class FabricChannelFactory {

    private String cryptoConfigBase;
    private final FabricConfig fabricConfig;

    public FabricChannelFactory(FabricConfig fabricConfig, String cryptoConfigBase) {
        this.cryptoConfigBase = cryptoConfigBase;
        this.fabricConfig = fabricConfig;
    }

    public static FabricChannelFactory factory(FabricConfig fabricConfig, String cryptoConfigBase) {
        return new FabricChannelFactory(fabricConfig, cryptoConfigBase);
    }

    public Channel getChannel(List<Endorser> endorsers, HFClient client, String channelName) throws IOException, InvalidArgumentException, TransactionException {
        if(client.getChannel(channelName) != null) {
            return client.getChannel(channelName);
        }

        OrdererConfig ordererConfig = fabricConfig.getNetwork().getOrderer();

        Orderer orderer = client.newOrderer(ordererConfig.getServerHostname(), ordererConfig.getUrl(), ordererConfig.getPeerTlsProperties(cryptoConfigBase));
        Channel channel = client.newChannel(channelName);
        addPeers(endorsers, channel, client);
        return channel.addOrderer(orderer).initialize();
    }

    private void addPeers(List<Endorser> endorsers, Channel channel, HFClient client) throws InvalidArgumentException, IOException {
        for(Endorser endorser : endorsers) {
            OrganisationConfig orgConfig = fabricConfig.getNetwork().getOrganisation(endorser.getOrganisation());
            PeerConfig peerConfig = orgConfig.getPeers().get(endorser.getPeer());

//            EventHub eventHub = client.newEventHub(peerConfig.getServerHostname(), peerConfig.getEvents());
            Peer peer = client.newPeer(peerConfig.getServerHostname(), peerConfig.getRequests(), peerConfig.getPeerTlsProperties(cryptoConfigBase));
//            channel.addEventHub(eventHub);
            channel.addPeer(peer);
        }
    }
}
