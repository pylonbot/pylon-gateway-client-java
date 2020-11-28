package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.service.CacheService;

public interface TypingStartEvent extends Event<TypingStartEvent> {

    default long getUserId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.TypingStartEvent event =
                (bot.pylon.proto.discord.v1.event.TypingStartEvent) this;
        return event.getPayload().getUserId();
    }
    default long getTimestamp() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.TypingStartEvent event =
                (bot.pylon.proto.discord.v1.event.TypingStartEvent) this;
        return event.getPayload().getTimestamp().getSeconds();
    }
    default Member getMember() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.TypingStartEvent event =
                (bot.pylon.proto.discord.v1.event.TypingStartEvent) this;
        return new Member(CacheService.getSingleton(), getBotId(), event.getPayload().getMember());
    }
    default Channel getChannel() {
        return CacheService.getSingleton().getChannel(getGuildId(), getChannelId());
    }
    default long getChannelId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.TypingStartEvent event =
                (bot.pylon.proto.discord.v1.event.TypingStartEvent) this;
        return event.getPayload().getChannelId();
    }

}
