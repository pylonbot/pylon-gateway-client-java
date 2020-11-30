package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Message;

public interface MessageUpdateEvent extends Event<MessageUpdateEvent> {

    default Message getMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageUpdateEvent) this;
        return new Message(GatewayGrpcClient.getSingleton(), getBotId(), event.getCached());
    }

    default Message getOldMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new Message(GatewayGrpcClient.getSingleton(), getBotId(), event.getPreviouslyCached());
    }
}
