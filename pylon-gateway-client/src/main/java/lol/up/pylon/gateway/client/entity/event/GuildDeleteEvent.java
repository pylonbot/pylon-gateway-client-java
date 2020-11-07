package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

import javax.annotation.Nonnull;

public interface GuildDeleteEvent extends Event<GuildDeleteEvent> {

    @Override
    @Nonnull
    default Guild getGuild() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildDeleteEvent event = (pylon.rpc.discord.v1.event.GuildDeleteEvent) this;
        return new Guild(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }
}
