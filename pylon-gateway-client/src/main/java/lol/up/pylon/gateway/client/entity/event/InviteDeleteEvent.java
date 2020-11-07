package lol.up.pylon.gateway.client.entity.event;

public interface InviteDeleteEvent extends Event<InviteDeleteEvent> {

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.InviteDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.InviteDeleteEvent event =
                (pylon.rpc.discord.v1.event.InviteDeleteEvent) this;
        return event.getPayload().getChannelId();
    }

    default String getInviteCode() {
        if (!(this instanceof pylon.rpc.discord.v1.event.InviteDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.InviteDeleteEvent event =
                (pylon.rpc.discord.v1.event.InviteDeleteEvent) this;
        return event.getPayload().getCode();
    }

}
