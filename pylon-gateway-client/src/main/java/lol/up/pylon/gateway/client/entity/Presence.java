package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.PresenceData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

public class Presence implements Entity<PresenceData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final PresenceData data;

    public Presence(final GatewayGrpcClient grpcClient, final long botId, final PresenceData data) {
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
        return data.getGuildId();
    }

    @Override
    public PresenceData getData() {
        return data;
    }

    // DATA
}
