package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.PresenceData;

public interface PresenceUpdateEvent extends Event<PresenceUpdateEvent> {

    default PresenceData getPresence() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.PresenceUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.PresenceUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.PresenceUpdateEvent) this;
        return event.getPayload(); // todo wrap nicely
    }

    default PresenceData getPreviousPresence() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.PresenceUpdateEvent)) {
            throw new IllegalStateException("GuildCreateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.PresenceUpdateEvent");
        }
        final bot.pylon.proto.discord.v1.event.PresenceUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.PresenceUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return event.getPreviousPresence(); // todo wrap nicely
    }
}
