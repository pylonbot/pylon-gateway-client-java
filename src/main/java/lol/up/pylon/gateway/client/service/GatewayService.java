package lol.up.pylon.gateway.client.service;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import rpc.gateway.v1.GatewayGrpc;
import rpc.gateway.v1.ShardKey;
import rpc.gateway.v1.TransferShardResponse;

public class GatewayService {

    private final GatewayGrpc.GatewayBlockingStub client;
    private final GatewayGrpcClient gatewayGrpcClient;

    public GatewayService(final GatewayGrpcClient gatewayGrpcClient, final GatewayGrpc.GatewayBlockingStub client) {
        this.client = client;
        this.gatewayGrpcClient = gatewayGrpcClient;
    }

    public TransferShardResponse restartShard(final int shardCount, final int shardId) {
        return restartShard(gatewayGrpcClient.getDefaultBotId(), shardCount, shardId);
    }

    public TransferShardResponse restartShard(final long botId, final int shardCount, final int shardId) {
        return client.transferShard(ShardKey.newBuilder()
                .setBotId(botId)
                .setShardCount(shardCount)
                .setShardId(shardId)
                .build());
    }

}
