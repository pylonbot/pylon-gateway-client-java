package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

public interface GuildUpdateEvent extends Event<GuildUpdateEvent> {

    @Override
    @CheckReturnValue
    default GrpcRequest<Guild> getGuild() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildUpdateEvent)) {
            throw new IllegalStateException("GuildUpdateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.GuildUpdateEvent");
        }
        final bot.pylon.proto.discord.v1.event.GuildUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.GuildUpdateEvent) this;
        return new FinishedRequestImpl<>(new Guild(GatewayGrpcClient.getSingleton(), event.getBotId(),
                event.getPayload()));
    }

    @Nullable
    default Guild getOld() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildUpdateEvent)) {
            throw new IllegalStateException("GuildUpdateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.GuildUpdateEvent");
        }
        final bot.pylon.proto.discord.v1.event.GuildUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.GuildUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new Guild(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }

}
