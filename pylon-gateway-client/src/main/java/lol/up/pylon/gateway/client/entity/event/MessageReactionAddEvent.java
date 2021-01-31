package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

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

    @Override
    default GrpcRequest<Channel> getChannel() {
        return GatewayGrpcClient.getSingleton().getCacheService().getChannel(getBotId(), getGuildId(), getChannelId());
    }

    @Override
    default GrpcRequest<User> getUser() {
        return GatewayGrpcClient.getSingleton().getCacheService().getUser(getBotId(), getUserId());
    }

    @Override
    @CheckReturnValue
    default GrpcRequest<Guild> getGuild() {
        if (getGuildId() == 0) {
            return null;
        }
        return GatewayGrpcClient.getSingleton().getCacheService().getGuild(getGuildId());
    }

    default long getGuildId() {
        return getScope().getGuildId();
    }
}
