package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

public interface MessageReactionRemoveEmojiEvent extends Event<MessageReactionRemoveEmojiEvent> {

    default long getChannelId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent) this;
        return event.getPayload().getChannelId();
    }

    default long getMessageId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent) this;
        return event.getPayload().getMessageId();
    }

    default MessageData.MessageReactionEmojiData getEmoji() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEmojiEvent) this;
        return event.getPayload().getEmoji();
    }

}
