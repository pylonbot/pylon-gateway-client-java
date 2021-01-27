package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.entity.Message;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.Nullable;

public interface MessageCreateEvent extends Event<MessageCreateEvent> {

    default Message getMessage() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.MessageCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.MessageCreateEvent event =
                (bot.pylon.proto.discord.v1.event.MessageCreateEvent) this;
        return new Message(GatewayGrpcClient.getSingleton(), getBotId(), event.getMessageData());
    }

    default long getChannelId() {
        return getMessage().getChannelId();
    }

    default GrpcRequest<Channel> getChannel() {
        return getMessage().getChannel();
    }

    default String getContent() {
        return getMessage().getContent();
    }

    default User getAuthor() {
        return getMessage().getAuthor();
    }

    @Nullable
    default Member getMember() {
        return getMessage().getMember();
    }

    default boolean isFromGuild() {
        return getGuildId() > 0;
    }

}
