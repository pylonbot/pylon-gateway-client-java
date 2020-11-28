package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Guild;

import javax.annotation.Nonnull;

public interface GuildDeleteEvent extends Event<GuildDeleteEvent> {

    @Override
    @Nonnull
    default Guild getGuild() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.GuildDeleteEvent) this;
        return new Guild(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }
}
