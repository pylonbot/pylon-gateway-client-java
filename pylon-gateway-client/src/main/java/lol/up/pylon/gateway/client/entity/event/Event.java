package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.event.EventScope;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Guild;

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
        return GatewayGrpcClient.getSingleton().getCacheService().getGuild(getGuildId());
    }
    default long getGuildId() {
        return getScope().getGuildId();
    }
}
