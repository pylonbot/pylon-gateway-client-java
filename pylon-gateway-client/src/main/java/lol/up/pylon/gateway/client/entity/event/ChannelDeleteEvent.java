package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.GuildChannel;

public interface ChannelDeleteEvent extends Event<ChannelDeleteEvent> {

    default GuildChannel getChannel() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.ChannelDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.ChannelDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.ChannelDeleteEvent) this;
        return new GuildChannel(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }

}
