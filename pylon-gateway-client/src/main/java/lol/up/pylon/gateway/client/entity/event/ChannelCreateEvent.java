package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface ChannelCreateEvent extends Event<ChannelCreateEvent> {

    default Channel getChannel() {
        if (!(this instanceof pylon.rpc.discord.v1.event.ChannelCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.ChannelCreateEvent event =
                (pylon.rpc.discord.v1.event.ChannelCreateEvent) this;
        return new Channel(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

}