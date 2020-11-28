package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.RoleData;

public class Role implements Entity<RoleData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final RoleData data;

    public Role(final GatewayGrpcClient grpcClient, final long botId, final RoleData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public CacheService getGatewayCacheService() {
        return grpcClient.getCacheService();
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return data.getGuildId();
    }

    @Override
    public RoleData getData() {
        return data;
    }

}
