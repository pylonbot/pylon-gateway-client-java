package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.MessageData;

public interface MessageReactionRemoveEmojiEvent extends Event<MessageReactionRemoveEmojiEvent> {

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent) this;
        return event.getPayload().getChannelId();
    }

    default long getMessageId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent) this;
        return event.getPayload().getMessageId();
    }

    default MessageData.MessageReactionEmojiData getEmoji() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveEmojiEvent) this;
        return event.getPayload().getEmoji();
    }

}
