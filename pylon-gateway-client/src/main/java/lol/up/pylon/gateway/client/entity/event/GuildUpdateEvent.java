package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface GuildUpdateEvent extends Event<GuildUpdateEvent> {

    @Override
    @Nonnull
    default Guild getGuild() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildUpdateEvent)) {
            throw new IllegalStateException("GuildUpdateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.GuildUpdateEvent");
        }
        final pylon.rpc.discord.v1.event.GuildUpdateEvent event = (pylon.rpc.discord.v1.event.GuildUpdateEvent) this;
        return new Guild(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

    @Nullable
    default Guild getOld() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildUpdateEvent)) {
            throw new IllegalStateException("GuildUpdateEvent interface might only be implemented by pylon.rpc" +
                    ".discord.v1.event.GuildUpdateEvent");
        }
        final pylon.rpc.discord.v1.event.GuildUpdateEvent event = (pylon.rpc.discord.v1.event.GuildUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new Guild(GatewayCacheService.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }

}