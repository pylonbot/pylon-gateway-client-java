package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.WebhookData;

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
    public CacheService getGatewayCacheService() {
        return grpcClient.getCacheService();
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return data.getGuildId();
    }

    @Override
    public WebhookData getData() {
        return data;
    }

    public Channel getChannel() {
        return getGatewayCacheService().getChannel(getBotId(), getGuildId(), getData().getChannelId());
    }

}
