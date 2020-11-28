package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.event.EventScope;

import javax.annotation.Nullable;

public interface Event<T extends Event> {

    Class<T> getInterfaceType();
    default long getBotId() {
        return getScope().getBotId();
    }
    EventScope getScope();
    @Nullable
    default Guild getGuild() {
        if (getGuildId() == 0) {
            return null;
        }
        return CacheService.getSingleton().getGuild(getGuildId());
    }
    default long getGuildId() {
        return getScope().getGuildId();
    }
}
