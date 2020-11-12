package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Role;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildRoleCreateEvent extends Event<GuildRoleCreateEvent> {

    default Role getRole() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildRoleCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildRoleCreateEvent event =
                (bot.pylon.proto.discord.v1.event.GuildRoleCreateEvent) this;
        return new Role(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }
}
