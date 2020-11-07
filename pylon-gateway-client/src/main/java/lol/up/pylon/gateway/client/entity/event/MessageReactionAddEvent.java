package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.MessageData;

public interface MessageReactionAddEvent extends Event<MessageReactionAddEvent> {

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionAddEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getChannelId();
    }

    default long getMessageId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionAddEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getMessageId();
    }

    default long getUserId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionAddEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getUserId();
    }

    default MessageData.MessageReactionEmojiData getEmoji() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionAddEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getEmoji();
    }

}
