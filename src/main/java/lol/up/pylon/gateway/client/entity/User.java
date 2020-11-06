package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.UserData;

public class User implements Entity<UserData> {

    private final GatewayCacheService cacheService;
    private final long botId;
    private final UserData data;

    public User(final GatewayCacheService cacheService, final long botId, final UserData data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public GatewayCacheService getGatewayCacheService() {
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
