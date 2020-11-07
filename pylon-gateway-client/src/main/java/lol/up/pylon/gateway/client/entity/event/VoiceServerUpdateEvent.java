package lol.up.pylon.gateway.client.entity.event;

public interface VoiceServerUpdateEvent extends Event<VoiceServerUpdateEvent> {

    default String getEndpoint() {
        if (!(this instanceof pylon.rpc.discord.v1.event.VoiceServerUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.VoiceServerUpdateEvent event =
                (pylon.rpc.discord.v1.event.VoiceServerUpdateEvent) this;
        return event.getPayload().getEndpoint();
    }

    default String getToken() {
        if (!(this instanceof pylon.rpc.discord.v1.event.VoiceServerUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.VoiceServerUpdateEvent event =
                (pylon.rpc.discord.v1.event.VoiceServerUpdateEvent) this;
        return event.getPayload().getToken();
    }

}
