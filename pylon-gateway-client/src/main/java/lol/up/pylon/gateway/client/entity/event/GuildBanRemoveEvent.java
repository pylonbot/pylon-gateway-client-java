package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.User;

public interface GuildBanRemoveEvent extends Event<GuildBanRemoveEvent> {

    default User getUser() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildBanRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildBanRemoveEvent event =
                (bot.pylon.proto.discord.v1.event.GuildBanRemoveEvent) this;
        return new User(GatewayGrpcClient.getSingleton(), getBotId(), event.getPayload().getUser());
    }

}
