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
 *
 * This event class can not be subscribed.
 */
public interface MessageReactionEvent {

    long getGuildId();

    long getChannelId();

    long getMessageId();

    long getUserId();

    MessageData.MessageReactionEmojiData getEmoji();

    @CheckReturnValue
    GrpcRequest<Guild> getGuild();

    @CheckReturnValue
    GrpcRequest<Channel> getChannel();

    @CheckReturnValue
    GrpcRequest<User> getUser();

}
