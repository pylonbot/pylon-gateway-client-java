package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.MessageData;

import javax.annotation.Nullable;

public interface MessageDeleteEvent extends Event<MessageDeleteEvent> {

    @Nullable
    default MessageData getCachedMessage() throws IllegalStateException {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.MessageDeleteEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return event.getPreviouslyCached(); // todo wrap nicely
    }
    default Channel getChannel() {
        return CacheService.getSingleton().getChannel(getGuildId(), getChannelId());
    }
    default long getChannelId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.MessageDeleteEvent) this;
        return event.getPayload().getChannelId();
    }
    default long getMessageId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.MessageDeleteEvent) this;
        return event.getPayload().getId();
    }

}
