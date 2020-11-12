package lol.up.pylon.gateway.client.entity.event;

import java.util.List;

public interface MessageDeleteBulkEvent extends Event<MessageDeleteBulkEvent> {

    default long getChannelId() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageDeleteBulkEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageDeleteBulkEvent event =
                (bot.pylon.proto.discord.v1.event.MessageDeleteBulkEvent) this;
        return event.getPayload().getChannelId();
    }

    default List<Long> getMessageIds() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageDeleteBulkEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageDeleteBulkEvent event =
                (bot.pylon.proto.discord.v1.event.MessageDeleteBulkEvent) this;
        return event.getPayload().getIdsList();
    }

}
