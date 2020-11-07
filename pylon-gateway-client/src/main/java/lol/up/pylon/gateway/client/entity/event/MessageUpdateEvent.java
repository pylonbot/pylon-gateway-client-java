package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.MessageData;

public interface MessageUpdateEvent extends Event<MessageUpdateEvent> {

    default MessageData getMessage() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageUpdateEvent event =
                (pylon.rpc.discord.v1.event.MessageUpdateEvent) this;
        return event.getPayload(); // todo wrap nicely
    }

    default MessageData getOldMessage() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageUpdateEvent event =
                (pylon.rpc.discord.v1.event.MessageUpdateEvent) this;
        return event.getPrevious(); // todo wrap nicely
    }
}
