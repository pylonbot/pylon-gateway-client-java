package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.GuildBanData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

public class Ban implements Entity<GuildBanData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final long guildId;
    private final GuildBanData data;

    public Ban(final GatewayGrpcClient grpcClient, final long botId, final long guildId, final GuildBanData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.guildId = guildId;
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
        return guildId;
    }

    @Override
    public GuildBanData getData() {
        return data;
    }

    // DATA

    public String getReason() {
        return getData().getReason();
    }

    public User getUser() {
        return new User(getClient(), getBotId(), getData().getUser());
    }
}
