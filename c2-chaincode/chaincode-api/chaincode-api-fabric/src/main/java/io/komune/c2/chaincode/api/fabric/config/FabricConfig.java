package io.komune.c2.chaincode.api.fabric.config;


import io.komune.c2.chaincode.api.fabric.utils.FileUtils;
import io.komune.c2.chaincode.api.fabric.utils.JsonUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.StringJoiner;

public class FabricConfig {

    public static FabricConfig loadFromFile(String filename) throws IOException {
        URL file = FileUtils.getUrl(filename);
        return JsonUtils.toObject(file, FabricConfig.class);
    }

    private NetworkConfig network;

    public FabricConfig() {
    }

    public FabricConfig(NetworkConfig network) {
        this.network = network;
    }

    public NetworkConfig getNetwork() {
        return network;
    }

    public FabricConfig setNetwork(NetworkConfig network) {
        this.network = network;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FabricConfig)) return false;
        FabricConfig that = (FabricConfig) o;
        return Objects.equals(network, that.network);
    }

    @Override
    public int hashCode() {
        return Objects.hash(network);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", FabricConfig.class.getSimpleName() + "[", "]")
                .add("network=" + network)
                .toString();
    }

}
