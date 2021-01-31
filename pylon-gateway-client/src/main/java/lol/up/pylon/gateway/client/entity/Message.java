package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MessageData;
import bot.pylon.proto.discord.v1.rest.EditMessageRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

public class Message implements Entity<MessageData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private MessageData data;

    public Message(final GatewayGrpcClient grpcClient, final long botId, final MessageData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public GatewayGrpcClient getClient() {
        return grpcClient;
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        if (data.getGuildId().isInitialized()) {
            return data.getGuildId().getValue();
        }
        return 0L;
    }

    @Override
    @CheckReturnValue
    public GrpcRequest<Guild> getGuild() {
        if (data.getGuildId().isInitialized()) {
            return getClient().getCacheService().getGuild(getBotId(), getGuildId());
        }
        return new FinishedRequestImpl<>(null);
    }

    @Override
    public MessageData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public long getChannelId() {
        return getData().getChannelId();
    }

    public String getContent() {
        return getData().getContent();
    }

    public User getAuthor() {
        return new User(getClient(), getBotId(), getData().getAuthor());
    }

    public Member getMember() {
        return new Member(getClient(), getBotId(), getData().getMember());
    }


    // REST

    @CheckReturnValue
    public GrpcRequest<Void> editMessage(final String message) {
        return getClient().getRestService().editMessage(getBotId(), getGuildId(), EditMessageRequest.newBuilder()
                .setChannelId(getChannelId())
                .setMessageId(getId())
                .setContent(message)
                .build()).transform(msg -> {
            this.data = msg.getData();
            return null;
        });
    }

    @CheckReturnValue
    public GrpcRequest<Void> editMessage(final MessageData.MessageEmbedData embed) {
        return getClient().getRestService().editMessage(getBotId(), getGuildId(), EditMessageRequest.newBuilder()
                .setChannelId(getChannelId())
                .setMessageId(getId())
                .setEmbed(embed)
                .build()).transform(msg -> {
            this.data = msg.getData();
            return null;
        });
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<Channel> getChannel() {
        if (getGuildId() > 0) {
            return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getChannelId());
        } else {
            return getClient().getCacheService().getDmChannel(getBotId(), getChannelId(), getAuthor().getId());
        }
    }
}
