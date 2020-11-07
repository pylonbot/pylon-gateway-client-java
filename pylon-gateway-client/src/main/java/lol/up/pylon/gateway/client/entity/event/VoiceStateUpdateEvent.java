package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.MemberVoiceState;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public interface VoiceStateUpdateEvent extends Event<VoiceStateUpdateEvent> {

    default MemberVoiceState getVoiceState() {
        if (!(this instanceof pylon.rpc.discord.v1.event.VoiceStateUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.VoiceStateUpdateEvent event =
                (pylon.rpc.discord.v1.event.VoiceStateUpdateEvent) this;
        return new MemberVoiceState(GatewayCacheService.getSingleton(), getBotId(), event.getPayload());
    }

    default MemberVoiceState getOldVoiceState() {
        if (!(this instanceof pylon.rpc.discord.v1.event.VoiceStateUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.VoiceStateUpdateEvent event =
                (pylon.rpc.discord.v1.event.VoiceStateUpdateEvent) this;
        return new MemberVoiceState(GatewayCacheService.getSingleton(), getBotId(), event.getPreviouslyCached());
    }

}
