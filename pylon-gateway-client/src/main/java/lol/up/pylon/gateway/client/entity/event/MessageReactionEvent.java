package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

/**
 * Shared event between {@link MessageReactionAddEvent MessageReactionAddEvent} and {@link MessageReactionRemoveEvent
 * MessageReactionRemoveEvent} since both share the same methods.
 * <p>
 * This event class can not be subscribed.
 */
public interface MessageReactionEvent {

    long getGuildId();

    long getChannelId();

    long getMessageId();

    long getUserId();

    MessageData.MessageReactionEmojiData getEmoji();

    /**
     * @return true if {@link MessageReactionEvent#getEmoji() MessageReactionEvent#getEmoji()} returns a standard emoji
     */
    boolean isEmoji();

    /**
     * @return true if {@link MessageReactionEvent#getEmoji() MessageReactionEvent#getEmoji()} returns a custom emote
     */
    boolean isEmote();

    @CheckReturnValue
    GrpcRequest<Guild> getGuild();

    @CheckReturnValue
    GrpcRequest<Channel> getChannel();

    @CheckReturnValue
    GrpcRequest<User> getUser();

}
