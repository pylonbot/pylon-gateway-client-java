package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface ChannelPinsUpdateEvent extends Event<ChannelPinsUpdateEvent> {

    default Channel getChannel() {
        if (!(this instanceof pylon.rpc.discord.v1.event.ChannelPinsUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.ChannelPinsUpdateEvent event =
                (pylon.rpc.discord.v1.event.ChannelPinsUpdateEvent) this;
        return GatewayCacheService.getSingleton().getChannel(event.getPayload().getGuildId(),
                event.getPayload().getChannelId());
    }

    default long getLastPinTimestamp() {
        if (!(this instanceof pylon.rpc.discord.v1.event.ChannelPinsUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.ChannelPinsUpdateEvent event =
                (pylon.rpc.discord.v1.event.ChannelPinsUpdateEvent) this;
        return event.getLastPinTimestamp();
    }
}
