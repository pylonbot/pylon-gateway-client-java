package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Role;
import lol.up.pylon.gateway.client.service.CacheService;

import javax.annotation.Nullable;

public interface GuildRoleUpdateEvent extends Event<GuildRoleUpdateEvent> {

    default Role getRole() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildRoleUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildRoleUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.GuildRoleUpdateEvent) this;
        return new Role(CacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

    @Nullable
    default Role getOldRole() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildRoleUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildRoleUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.GuildRoleUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new Role(CacheService.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }

}
