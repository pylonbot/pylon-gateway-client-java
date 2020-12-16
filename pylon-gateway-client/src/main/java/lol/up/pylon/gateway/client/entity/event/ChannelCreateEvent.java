package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.GuildChannel;

public interface ChannelCreateEvent extends Event<ChannelCreateEvent> {

    default GuildChannel getChannel() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.ChannelCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.ChannelCreateEvent event =
                (bot.pylon.proto.discord.v1.event.ChannelCreateEvent) this;
        return new GuildChannel(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }

}
