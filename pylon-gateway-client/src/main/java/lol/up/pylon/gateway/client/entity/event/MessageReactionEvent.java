package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

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

    GrpcRequest<Channel> getChannel();

    GrpcRequest<User> getUser();

}
