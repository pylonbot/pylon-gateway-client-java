package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface TypingStartEvent extends Event<TypingStartEvent> {

    default long getUserId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.TypingStartEvent event =
                (pylon.rpc.discord.v1.event.TypingStartEvent) this;
        return event.getPayload().getUserId();
    }
    default long getTimestamp() {
        if (!(this instanceof pylon.rpc.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.TypingStartEvent event =
                (pylon.rpc.discord.v1.event.TypingStartEvent) this;
        return event.getPayload().getTimestamp();
    }
    default Member getMember() {
        if (!(this instanceof pylon.rpc.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.TypingStartEvent event =
                (pylon.rpc.discord.v1.event.TypingStartEvent) this;
        return new Member(GatewayCacheService.getSingleton(), getBotId(), event.getPayload().getMember());
    }
    default Channel getChannel() {
        return GatewayCacheService.getSingleton().getChannel(getGuildId(), getChannelId());
    }
    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.TypingStartEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.TypingStartEvent event =
                (pylon.rpc.discord.v1.event.TypingStartEvent) this;
        return event.getPayload().getChannelId();
    }

}
