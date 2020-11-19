package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

public interface MessageUpdateEvent extends Event<MessageUpdateEvent> {

    default MessageData getMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageUpdateEvent) this;
        return event.getCached(); // todo wrap nicely
    }

    default MessageData getOldMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return event.getPreviouslyCached(); // todo wrap nicely
    }
}
