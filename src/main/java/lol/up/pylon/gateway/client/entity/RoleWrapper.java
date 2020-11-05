package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.Role;

public class RoleWrapper implements WrappedEntity<Role> {

    private final long botId;
    private final Role data;
    private final GatewayCacheService cacheService;

    public RoleWrapper(final GatewayCacheService cacheService, final long botId, final Role data) {
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
    public Role getData() {
        return data;
    }

}
