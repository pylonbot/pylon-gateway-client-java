package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Role;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildRoleUpdateEvent extends Event<GuildRoleUpdateEvent> {

    default Role getRole() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildRoleUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildRoleUpdateEvent event =
                (pylon.rpc.discord.v1.event.GuildRoleUpdateEvent) this;
        return new Role(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

    default Role getOldRole() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildRoleUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildRoleUpdateEvent event =
                (pylon.rpc.discord.v1.event.GuildRoleUpdateEvent) this;
        return new Role(GatewayCacheService.getSingleton(), event.getBotId(), event.getPrevious());
    }
}
