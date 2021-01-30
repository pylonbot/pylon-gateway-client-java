package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

public interface MessageReactionAddEvent extends Event<MessageReactionAddEvent>, MessageReactionEvent {

    @Override
    default long getChannelId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionAddEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getChannelId();
    }

    @Override
    default long getMessageId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionAddEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getMessageId();
    }

    @Override
    default long getUserId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionAddEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getUserId();
    }

    @Override
    default MessageData.MessageReactionEmojiData getEmoji() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionAddEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionAddEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionAddEvent) this;
        return event.getPayload().getEmoji();
    }

}
