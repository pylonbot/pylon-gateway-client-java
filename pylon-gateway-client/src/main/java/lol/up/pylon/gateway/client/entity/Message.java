package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

public class Message implements Entity<MessageData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final MessageData data;

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
        return -1L;
    }

    @Override
    public GrpcRequest<Guild> getGuild() {
        if (data.getGuildId().isInitialized()) {
            return getClient().getCacheService().getGuild(getBotId(), getGuildId());
        }
        return null; // todo: not null
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
        return new User(getClient(), botId, getData().getAuthor());
    }

    public Member getMember() {
        return new Member(getClient(), botId, getData().getMember());
    }


    // REST

    // CACHE

    public GrpcRequest<Channel> getChannel() {
        return getClient().getCacheService().getChannel(botId, getGuildId(), getChannelId());
    }
}
