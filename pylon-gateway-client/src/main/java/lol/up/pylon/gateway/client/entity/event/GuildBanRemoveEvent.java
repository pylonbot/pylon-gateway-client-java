package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildBanRemoveEvent extends Event<GuildBanRemoveEvent> {

    default User getUser() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildBanRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildBanRemoveEvent event =
                (pylon.rpc.discord.v1.event.GuildBanRemoveEvent) this;
        return new User(GatewayCacheService.getSingleton(), getBotId(), event.getPayload().getUser());
    }

}
