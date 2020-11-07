package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildBanAddEvent extends Event<GuildBanAddEvent> {

    default User getUser() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildBanAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildBanAddEvent event =
                (pylon.rpc.discord.v1.event.GuildBanAddEvent) this;
        return new User(GatewayCacheService.getSingleton(), getBotId(), event.getPayload().getUser());
    }

}
