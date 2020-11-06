package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface Entity<E> {

    GatewayCacheService getGatewayCacheService();
    long getBotId();
    long getGuildId();
    E getData();

    default Guild getGuild() {
        return getGatewayCacheService().getGuild(getBotId(), getGuildId());
    }
}
