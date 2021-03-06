package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

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

    /**
     * @return true if {@link MessageReactionRemoveEvent#getEmoji() MessageReactionRemoveEvent#getEmoji()}
     * returns a standard emoji
     */
    default boolean isEmoji() {
        return getEmoji().getId() == 0L;
    }

    /**
     * @return true if {@link MessageReactionRemoveEvent#getEmoji() MessageReactionRemoveEvent#getEmoji()}
     * returns a custom emote
     */
    default boolean isEmote() {
        return getEmoji().getId() > 0L;
    }

    @Override
    @CheckReturnValue
    default GrpcRequest<Channel> getChannel() {
        return GatewayGrpcClient.getSingleton().getCacheService().getChannel(getBotId(), getGuildId(), getChannelId());
    }

    @Override
    @CheckReturnValue
    default GrpcRequest<User> getUser() {
        return GatewayGrpcClient.getSingleton().getCacheService().getUser(getBotId(), getGuildId(), getUserId());
    }

    @Override
    @CheckReturnValue
    default GrpcRequest<Guild> getGuild() {
        if (getGuildId() == 0) {
            return null;
        }
        return GatewayGrpcClient.getSingleton().getCacheService().getGuild(getGuildId());
    }

    @Override
    default long getGuildId() {
        return getScope().getGuildId();
    }
}
