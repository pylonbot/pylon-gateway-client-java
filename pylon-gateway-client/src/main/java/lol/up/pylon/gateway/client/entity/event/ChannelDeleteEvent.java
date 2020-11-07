package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface ChannelDeleteEvent extends Event<ChannelDeleteEvent> {

    default Channel getChannel() {
        if (!(this instanceof pylon.rpc.discord.v1.event.ChannelDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.ChannelDeleteEvent event =
                (pylon.rpc.discord.v1.event.ChannelDeleteEvent) this;
        return new Channel(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

}
