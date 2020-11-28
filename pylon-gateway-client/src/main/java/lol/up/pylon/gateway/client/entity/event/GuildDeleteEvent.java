package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.CacheService;

import javax.annotation.Nonnull;

public interface GuildDeleteEvent extends Event<GuildDeleteEvent> {

    @Override
    @Nonnull
    default Guild getGuild() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildDeleteEvent event = (bot.pylon.proto.discord.v1.event.GuildDeleteEvent) this;
        return new Guild(CacheService.getSingleton(), event.getBotId(), event.getPayload());
    }
}
