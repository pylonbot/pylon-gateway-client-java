package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.CacheService;

import javax.annotation.Nonnull;

public interface GuildCreateEvent extends Event<GuildCreateEvent> {

    @Override
    @Nonnull
    default Guild getGuild() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildCreateEvent event = (bot.pylon.proto.discord.v1.event.GuildCreateEvent) this;
        return new Guild(CacheService.getSingleton(), event.getBotId(), event.getPayload());
    }
}
