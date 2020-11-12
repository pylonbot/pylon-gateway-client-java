package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import bot.pylon.proto.discord.v1.model.RoleData;

public class Role implements Entity<RoleData> {

    private final long botId;
    private final RoleData data;
    private final GatewayCacheService cacheService;

    public Role(final GatewayCacheService cacheService, final long botId, final RoleData data) {
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
        return data.getGuildId();
    }

    @Override
    public RoleData getData() {
        return data;
    }

}
