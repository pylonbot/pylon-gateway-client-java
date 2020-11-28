package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Member;

public interface GuildMemberAddEvent extends Event<GuildMemberAddEvent> {

    default Member getMember() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildMemberAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildMemberAddEvent event =
                (bot.pylon.proto.discord.v1.event.GuildMemberAddEvent) this;
        return new Member(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }

}
