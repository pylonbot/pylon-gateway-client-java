package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildCreateEvent extends Event<GuildCreateEvent> {

    default Guild getGuild() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildCreateEvent event = (pylon.rpc.discord.v1.event.GuildCreateEvent) this;
        return new Guild(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }
}
