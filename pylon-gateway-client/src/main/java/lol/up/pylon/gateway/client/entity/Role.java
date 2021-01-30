package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.RoleData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

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
    public GatewayGrpcClient getClient() {
        return grpcClient;
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return getData().getGuildId();
    }

    @Override
    public RoleData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public int getPosition() {
        return getData().getPosition();
    }

    public long getPermissions() {
        return getData().getPermissions();
    }

    public boolean isPublicRole() {
        return getGuildId() == getId();
    }

    public String getName() {
        return getData().getName();
    }

}
