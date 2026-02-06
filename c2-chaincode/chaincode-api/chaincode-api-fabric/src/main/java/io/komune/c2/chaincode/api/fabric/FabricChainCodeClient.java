package io.komune.c2.chaincode.api.fabric;

import io.komune.c2.chaincode.api.fabric.config.FabricConfig;
import io.komune.c2.chaincode.api.fabric.exception.InvokeException;
import io.komune.c2.chaincode.api.fabric.factory.FabricChannelFactory;
import io.komune.c2.chaincode.api.fabric.model.Endorser;
import io.komune.c2.chaincode.api.fabric.model.InvokeArgs;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class FabricChainCodeClient {

    private final Logger logger = LoggerFactory.getLogger(FabricChainCodeClient.class);

    public static FabricChainCodeClient fromConfigFile(String filename, String cryptoConfigBase) throws IOException {
        FabricConfig fabricConfig = FabricConfig.loadFromFile(filename);
        FabricChannelFactory channelFactory = FabricChannelFactory.factory(fabricConfig, cryptoConfigBase);

        return new FabricChainCodeClient(channelFactory);
    }

    private final FabricChannelFactory channelFactory;


    public FabricChainCodeClient(FabricChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public CompletableFuture<BlockEvent.TransactionEvent> invoke(List<Endorser> endorsers, HFClient client, String channelName, String chainId, InvokeArgs invokeArgs) throws Exception {
        Channel channel = channelFactory.getChannel(endorsers, client, channelName);
        ChaincodeID chainCodeId = ChaincodeID.newBuilder().setName(chainId).build();
        return invokeBlockChain(client, channel, chainCodeId, invokeArgs);
    }

    public String query(List<Endorser> endorsers, HFClient client, String channelName, String chainId, InvokeArgs invokeArgs) throws Exception {
        Channel channel = channelFactory.getChannel(endorsers, client, channelName);
        ChaincodeID chainCodeId = ChaincodeID.newBuilder().setName(chainId).build();
        return queryBlockChain(client, channel, chainCodeId, invokeArgs);
    }

    private CompletableFuture<BlockEvent.TransactionEvent> invokeBlockChain(HFClient client, Channel channel, ChaincodeID chainCodeId, InvokeArgs invokeArgs) throws InvokeException {
        try {
            TransactionProposalRequest qpr = buildTransactionProposalRequest(client, chainCodeId, invokeArgs);
            Collection<ProposalResponse> responses = channel.sendTransactionProposal(qpr, channel.getPeers());
            List<String> errors = checkProposals(responses);

            if(errors.size() >= responses.size()) {
                StringJoiner joiner = new StringJoiner(",");
                errors.forEach(joiner::add);
                logger.error("Transaction[{}] errors: {}", invokeArgs.getFunction(), joiner);
                throw new InvokeException(errors);
            }
            return channel.sendTransaction(responses);
        } catch (ProposalException | InvalidArgumentException e) {
            throw new InvokeException(e);
        }
    }

    private TransactionProposalRequest buildTransactionProposalRequest(HFClient client, ChaincodeID chainCodeId, InvokeArgs invokeArgs) {
        TransactionProposalRequest qpr = client.newTransactionProposalRequest();
        qpr.setChaincodeID(chainCodeId);
        qpr.setFcn(invokeArgs.getFunction());
        qpr.setArgs(invokeArgs.getValues());
        return qpr;
    }

    private List<String> checkProposals(Collection<ProposalResponse> responses) {
        List<String> errors = new ArrayList<>();
        for(ProposalResponse res : responses) {
            if(res.isInvalid()){
                errors.add(res.getMessage());
            }
        }
        return errors;
    }

    private String queryBlockChain(HFClient client, Channel channel, ChaincodeID chanCodeId, InvokeArgs invokeArgs) throws ProposalException, InvalidArgumentException {
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        qpr.setChaincodeID(chanCodeId);
        qpr.setFcn(invokeArgs.getFunction());
        qpr.setArgs(invokeArgs.getValues());
        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);
        return new String(res.iterator().next().getChaincodeActionResponsePayload());
    }
}
