package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.UserData;

public class User implements Entity<UserData> {

    private final CacheService cacheService;
    private final long botId;
    private final UserData data;

    public User(final CacheService cacheService, final long botId, final UserData data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public CacheService getGatewayCacheService() {
        return cacheService;
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
}
