package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;

public interface Entity<E> {

    CacheService getGatewayCacheService();
    long getBotId();
    long getGuildId();
    E getData();

    default Guild getGuild() {
        return getGatewayCacheService().getGuild(getBotId(), getGuildId());
    }
}
