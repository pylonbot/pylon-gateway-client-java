package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.MemberVoiceState;
import lol.up.pylon.gateway.client.service.CacheService;

public interface VoiceStateUpdateEvent extends Event<VoiceStateUpdateEvent> {

    default MemberVoiceState getVoiceState() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.VoiceStateUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.VoiceStateUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.VoiceStateUpdateEvent) this;
        return new MemberVoiceState(CacheService.getSingleton(), getBotId(), event.getPayload());
    }

    default MemberVoiceState getOldVoiceState() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.VoiceStateUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.VoiceStateUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.VoiceStateUpdateEvent) this;
        return new MemberVoiceState(CacheService.getSingleton(), getBotId(), event.getPreviouslyCached());
    }

}
