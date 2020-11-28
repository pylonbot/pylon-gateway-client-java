package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.service.CacheService;

public interface GuildMemberRemoveEvent extends Event<GuildMemberRemoveEvent> {

    default Member getMember() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildMemberRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildMemberRemoveEvent event =
                (bot.pylon.proto.discord.v1.event.GuildMemberRemoveEvent) this;
        return new Member(CacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

}
