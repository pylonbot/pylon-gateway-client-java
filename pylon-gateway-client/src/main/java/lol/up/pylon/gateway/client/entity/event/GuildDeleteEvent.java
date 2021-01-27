package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

public interface GuildDeleteEvent extends Event<GuildDeleteEvent> {

    @Override
    @CheckReturnValue
    default GrpcRequest<Guild> getGuild() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.GuildDeleteEvent) this;
        return new FinishedRequestImpl<>(new Guild(GatewayGrpcClient.getSingleton(), event.getBotId(),
                event.getPayload()));
    }
}
