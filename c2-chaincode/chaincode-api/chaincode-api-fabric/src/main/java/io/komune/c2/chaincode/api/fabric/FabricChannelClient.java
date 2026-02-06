package io.komune.c2.chaincode.api.fabric;

import io.komune.c2.chaincode.api.fabric.config.FabricConfig;
import io.komune.c2.chaincode.api.fabric.factory.FabricChannelFactory;
import io.komune.c2.chaincode.api.fabric.model.Endorser;
import java.io.IOException;
import java.util.List;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.TransactionInfo;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

public class FabricChannelClient {

    public static FabricChannelClient fromConfigFile(String filename, String cryptoConfigBase) throws IOException {
        FabricConfig fabricConfig = FabricConfig.loadFromFile(filename);
        FabricChannelFactory channelFactory = FabricChannelFactory.factory(fabricConfig, cryptoConfigBase);

        return new FabricChannelClient(channelFactory);
    }

    private final FabricChannelFactory channelFactory;


    public FabricChannelClient(FabricChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }


    public TransactionInfo queryTransactionByID(List<Endorser> endorsers, HFClient client, String channelName, String txID) throws ProposalException, InvalidArgumentException, TransactionException, IOException {
        Channel channel = channelFactory.getChannel(endorsers, client, channelName);
        return channel.queryTransactionByID(txID, client.getUserContext());
    }

    public BlockInfo queryBlockByTransactionId(List<Endorser> endorsers, HFClient client, String channelName, String txID) throws ProposalException, InvalidArgumentException, TransactionException, IOException {
        Channel channel = channelFactory.getChannel(endorsers, client, channelName);
        return channel.queryBlockByTransactionID(txID, client.getUserContext());
    }

    public BlockInfo queryBlockByNumber(List<Endorser> endorsers, HFClient client, String channelName, long blockNumber) throws ProposalException, InvalidArgumentException, TransactionException, IOException {
        Channel channel = channelFactory.getChannel(endorsers, client, channelName);
        return channel.queryBlockByNumber(blockNumber, client.getUserContext());
    }

    public long queryBlockCount(List<Endorser> endorsers, HFClient client, String channelName) throws ProposalException, InvalidArgumentException, TransactionException, IOException {
        Channel channel = channelFactory.getChannel(endorsers, client, channelName);
        return channel.queryBlockchainInfo().getHeight();
    }
}
