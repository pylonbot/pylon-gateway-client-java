package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;

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
    public CacheService getGatewayCacheService() {
        return grpcClient.getCacheService();
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
    public Guild getGuild() {
        if (data.getGuildId().isInitialized()) {
            return getGatewayCacheService().getGuild(getBotId(), getGuildId());
        }
        return null;
    }

    @Override
    public MessageData getData() {
        return data;
    }

    public User getAuthor() {
        return new User(grpcClient, botId, data.getAuthor());
    }

    public Member getMember() {
        return new Member(grpcClient, botId, data.getMember());
    }

    public String getContent() {
        return data.getContent();
    }
}
