package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Presence;

public interface PresenceUpdateEvent extends Event<PresenceUpdateEvent> {

    default Presence getPresence() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.PresenceUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.PresenceUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.PresenceUpdateEvent) this;
        return new Presence(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }

    default Presence getPreviousPresence() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.PresenceUpdateEvent)) {
            throw new IllegalStateException("GuildCreateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.PresenceUpdateEvent");
        }
        final bot.pylon.proto.discord.v1.event.PresenceUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.PresenceUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new Presence(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }
}
