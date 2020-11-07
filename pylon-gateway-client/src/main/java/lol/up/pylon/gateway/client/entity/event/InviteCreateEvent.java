package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.InviteData;

public interface InviteCreateEvent extends Event<InviteCreateEvent> {

    default InviteData getInvite() {
        if (!(this instanceof pylon.rpc.discord.v1.event.InviteCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.InviteCreateEvent event =
                (pylon.rpc.discord.v1.event.InviteCreateEvent) this;
        return event.getPayload();
    }

}
