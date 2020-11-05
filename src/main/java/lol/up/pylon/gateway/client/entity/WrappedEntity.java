package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface WrappedEntity<E> {

    GatewayCacheService getGatewayCacheService();
    long getBotId();
    long getGuildId();
    E getData();

    default GuildWrapper getGuild() {
        return getGatewayCacheService().getGuild(getBotId(), getGuildId());
    }
}
