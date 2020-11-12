package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

public interface MessageCreateEvent extends Event<MessageCreateEvent> {

    default MessageData getMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageCreateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageCreateEvent) this;
        return event.getMessage(); // todo wrap nicely
    }

}
