package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildMemberAddEvent extends Event<GuildMemberAddEvent> {

    default Member getMember() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildMemberAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildMemberAddEvent event =
                (bot.pylon.proto.discord.v1.event.GuildMemberAddEvent) this;
        return new Member(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

}
