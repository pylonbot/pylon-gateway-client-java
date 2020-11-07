package lol.up.pylon.gateway.client.entity.event;

import java.util.List;

public interface MessageDeleteBulkEvent extends Event<MessageDeleteBulkEvent> {

    default long getChannelId() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageDeleteBulkEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageDeleteBulkEvent event =
                (pylon.rpc.discord.v1.event.MessageDeleteBulkEvent) this;
        return event.getPayload().getChannelId();
    }

    default List<Long> getMessageIds() {
        if (!(this instanceof pylon.rpc.discord.v1.event.MessageDeleteBulkEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.MessageDeleteBulkEvent event =
                (pylon.rpc.discord.v1.event.MessageDeleteBulkEvent) this;
        return event.getPayload().getIdsList();
    }

}
