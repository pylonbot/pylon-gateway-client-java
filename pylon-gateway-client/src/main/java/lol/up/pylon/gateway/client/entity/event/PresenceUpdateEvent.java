package lol.up.pylon.gateway.client.entity.event;

import pylon.rpc.discord.v1.model.PresenceData;

public interface PresenceUpdateEvent extends Event<PresenceUpdateEvent> {

    default PresenceData getPresence() {
        if (!(this instanceof pylon.rpc.discord.v1.event.PresenceUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.PresenceUpdateEvent event =
                (pylon.rpc.discord.v1.event.PresenceUpdateEvent) this;
        return event.getPayload(); // todo wrap nicely
    }

    default PresenceData getPreviousPresence() {
        if (!(this instanceof pylon.rpc.discord.v1.event.PresenceUpdateEvent)) {
            throw new IllegalStateException("GuildCreateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.PresenceUpdateEvent");
        }
        final pylon.rpc.discord.v1.event.PresenceUpdateEvent event =
                (pylon.rpc.discord.v1.event.PresenceUpdateEvent) this;
        return event.getPreviousPresence(); // todo wrap nicely
    }
}
