package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.MessageData;

public interface MessageCreateEvent extends Event<MessageCreateEvent> {

    default MessageData getMessage() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageCreateEvent event =
                (pylon.rpc.discord.v1.event.MessageCreateEvent) this;
        return event.getMessage(); // todo wrap nicely
    }

}
