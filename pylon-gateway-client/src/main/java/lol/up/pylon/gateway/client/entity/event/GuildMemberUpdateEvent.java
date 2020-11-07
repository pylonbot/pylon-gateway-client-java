package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

import javax.annotation.Nullable;

public interface GuildMemberUpdateEvent extends Event<GuildMemberUpdateEvent> {

    default Member getMember() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildMemberUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildMemberUpdateEvent event =
                (pylon.rpc.discord.v1.event.GuildMemberUpdateEvent) this;
        return new Member(GatewayCacheService.getSingleton(), event.getBotId(), event.getPayload());
    }

    @Nullable
    default Member getOldMember() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildMemberUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildMemberUpdateEvent event =
                (pylon.rpc.discord.v1.event.GuildMemberUpdateEvent) this;
        if (!event.hasPreviouslyCached()) {
            return null;
        }
        return new Member(GatewayCacheService.getSingleton(), event.getBotId(), event.getPreviouslyCached());
    }

}
