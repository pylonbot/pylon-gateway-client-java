package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.RoleData;

public class Role implements Entity<RoleData> {

    private final long botId;
    private final RoleData data;
    private final CacheService cacheService;

    public Role(final CacheService cacheService, final long botId, final RoleData data) {
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
        return data.getGuildId();
    }

    @Override
    public RoleData getData() {
        return data;
    }

}
