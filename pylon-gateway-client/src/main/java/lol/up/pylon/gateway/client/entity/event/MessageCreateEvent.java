package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Message;

public interface MessageCreateEvent extends Event<MessageCreateEvent> {

    default Message getMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageCreateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageCreateEvent) this;
        return new Message(GatewayGrpcClient.getSingleton(), getBotId(), event.getMessage());
    }

}
