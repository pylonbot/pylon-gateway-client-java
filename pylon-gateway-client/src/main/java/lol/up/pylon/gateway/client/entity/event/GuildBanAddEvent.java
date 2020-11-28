package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.CacheService;

public interface GuildBanAddEvent extends Event<GuildBanAddEvent> {

    default User getUser() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildBanAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildBanAddEvent event =
                (bot.pylon.proto.discord.v1.event.GuildBanAddEvent) this;
        return new User(CacheService.getSingleton(), getBotId(), event.getPayload().getUser());
    }

}
