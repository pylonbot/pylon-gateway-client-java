package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface GuildUpdateEvent extends Event<GuildUpdateEvent> {

    default Guild getGuild() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildUpdateEvent)) {
            throw new IllegalStateException("GuildUpdateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.GuildUpdateEvent");
        }
        final pylon.rpc.discord.v1.event.GuildUpdateEvent event = (pylon.rpc.discord.v1.event.GuildUpdateEvent) this;
        return new Guild(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

    default Guild getOld() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildUpdateEvent)) {
            throw new IllegalStateException("GuildUpdateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.GuildUpdateEvent");
        }
        final pylon.rpc.discord.v1.event.GuildUpdateEvent event = (pylon.rpc.discord.v1.event.GuildUpdateEvent) this;
        return new Guild(GatewayCacheService.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }

}
