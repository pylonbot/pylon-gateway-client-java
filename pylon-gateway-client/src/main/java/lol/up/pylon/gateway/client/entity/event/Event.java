package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.event.EventScope;

public interface Event<T extends Event> {

    EventScope getScope();

    Class<T> getInterfaceType();

    default long getBotId() {
        return getScope().getBotId();
    }

    default long getGuildId() {
        return getScope().getGuildId();
    }
}
