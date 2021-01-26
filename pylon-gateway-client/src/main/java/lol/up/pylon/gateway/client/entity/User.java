package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.UserData;

public class User implements Entity<UserData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final UserData data;

    public User(final GatewayGrpcClient grpcClient, final long botId, final UserData data) {
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
        throw new RuntimeException("Can't get a guildId on a user object");
    }

    @Override
    public UserData getData() {
        return data;
    }

    public String getName() {
        return data.getUsername();
    }

    public long getUserId() {
        return getData().getId();
    }

    public Member getAsMember(final Guild guild) {
        return getAsMember(guild.getGuildId());
    }

    public Member getAsMember(final long guildId) {
        return getClient().getCacheService().getMember(botId, guildId, getData().getId());
    }
}
