package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.MessageData;

public interface MessageReactionRemoveEvent extends Event<MessageReactionRemoveEvent> {

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getChannelId();
    }

    default long getMessageId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getMessageId();
    }

    default long getUserId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getUserId();
    }

    default MessageData.MessageReactionEmojiData getEmoji() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getEmoji();
    }

}
