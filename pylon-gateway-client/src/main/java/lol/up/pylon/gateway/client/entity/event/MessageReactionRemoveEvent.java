package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

public interface MessageReactionRemoveEvent extends Event<MessageReactionRemoveEvent>, MessageReactionEvent {

    @Override
    default long getChannelId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getChannelId();
    }

    @Override
    default long getMessageId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getMessageId();
    }

    @Override
    default long getUserId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getUserId();
    }

    @Override
    default MessageData.MessageReactionEmojiData getEmoji() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent event =
                (bot.pylon.proto.discord.v1.event.MessageReactionRemoveEvent) this;
        return event.getPayload().getEmoji();
    }

}
