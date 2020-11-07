package lol.up.pylon.gateway.client.entity.event;

public interface MessageReactionRemoveAllEvent extends Event<MessageReactionRemoveAllEvent> {

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveAllEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveAllEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveAllEvent) this;
        return event.getPayload().getChannelId();
    }

    default long getMessageId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageReactionRemoveAllEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageReactionRemoveAllEvent event =
                (pylon.rpc.discord.v1.event.MessageReactionRemoveAllEvent) this;
        return event.getPayload().getMessageId();
    }

}
