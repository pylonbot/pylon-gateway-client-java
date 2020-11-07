package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.service.GatewayCacheService;
import pylon.rpc.discord.v1.model.MessageData;

public interface MessageDeleteEvent extends Event<MessageDeleteEvent> {

    default MessageData getCachedMessage() throws IllegalStateException {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageDeleteEvent event =
                (pylon.rpc.discord.v1.event.MessageDeleteEvent) this;
        if (!event.hasCached()) {
            throw new IllegalStateException("No cached message");
        }
        return event.getCached(); // todo wrap nicely
    }

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageDeleteEvent event =
                (pylon.rpc.discord.v1.event.MessageDeleteEvent) this;
        if (event.hasCached()) {
            return event.getCached().getChannelId();
        } else {
            return event.getUncached().getChannelId();
        }
    }

    default Channel getChannel() {
        return GatewayCacheService.getSingleton().getChannel(getGuildId(), getChannelId());
    }

    default long getMessageId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageDeleteEvent event =
                (pylon.rpc.discord.v1.event.MessageDeleteEvent) this;
        if (event.hasCached()) {
            return event.getCached().getMessageReference().getMessageId();
        } else {
            return event.getUncached().getId();
        }
    }

}
