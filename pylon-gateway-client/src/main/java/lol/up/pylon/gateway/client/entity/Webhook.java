package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.WebhookData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

public class Webhook implements Entity<WebhookData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final WebhookData data;

    public Webhook(final GatewayGrpcClient grpcClient, final long botId, final WebhookData data) {
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
        return getData().getGuildId();
    }

    @Override
    public WebhookData getData() {
        return data;
    }

    @CheckReturnValue
    public GrpcRequest<Channel> getChannel() {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getData().getChannelId());
    }

}
