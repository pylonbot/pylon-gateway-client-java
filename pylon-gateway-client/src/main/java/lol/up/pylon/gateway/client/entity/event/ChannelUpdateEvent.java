package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.GuildChannel;

import javax.annotation.Nullable;

public interface ChannelUpdateEvent extends Event<ChannelUpdateEvent> {

    default GuildChannel getChannel() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.ChannelUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.ChannelUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.ChannelUpdateEvent) this;
        return new GuildChannel(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }

    @Nullable
    default GuildChannel getOldChannel() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.ChannelUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.ChannelUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.ChannelUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new GuildChannel(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }

}
