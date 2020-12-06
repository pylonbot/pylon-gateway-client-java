package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.InviteData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

public class GuildInvite implements Entity<InviteData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private InviteData data;

    public GuildInvite(final GatewayGrpcClient grpcClient, final long botId, final InviteData data) {
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
        return getData().getGuild().getId();
    }

    @Override
    public InviteData getData() {
        return data;
    }
}
