package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

/**
 * Shared event between {@link MessageReactionAddEvent MessageReactionAddEvent} and {@link MessageReactionRemoveEvent
 * MessageReactionRemoveEvent} since both share the same methods.
 *
 * This event class can not be subscribed.
 */
public interface MessageReactionEvent {

    long getChannelId();

    long getMessageId();

    long getUserId();

    MessageData.MessageReactionEmojiData getEmoji();

}
