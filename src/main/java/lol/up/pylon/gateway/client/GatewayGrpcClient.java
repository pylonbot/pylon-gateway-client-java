package lol.up.pylon.gateway.client;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pylon.rpc.gateway.v1.cache.GatewayCacheGrpc;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GatewayGrpcClient implements Closeable {

    private final ManagedChannel channel;
    private final GatewayCacheService cacheService;

    private long defaultBotId;

    public GatewayGrpcClient(final long defaultBotId, final String host, final int port, boolean enableRetry) {
        this(defaultBotId, enableRetry ?
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .enableRetry()
                        .build() :
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .build());
    }

    public GatewayGrpcClient(final long defaultBotId, final ManagedChannel channel) {
        this.channel = channel;
        this.cacheService = new GatewayCacheService(this, GatewayCacheGrpc.newBlockingStub(channel));
        this.defaultBotId = defaultBotId;
    }

    public void setDefaultBotId(final long defaultBotId) {
        this.defaultBotId = defaultBotId;
    }

    public long getDefaultBotId() {
        return defaultBotId;
    }

    public GatewayCacheService getCacheService() {
        return cacheService;
    }

    @Override
    public void close() throws IOException {
        try {
            this.channel.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Couldn't await channel termination in time", e);
        }
    }
}
