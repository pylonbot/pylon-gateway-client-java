package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import bot.pylon.proto.discord.v1.model.WebhookData;

public class Webhook implements Entity<WebhookData> {

    private final long botId;
    private final WebhookData data;
    private final GatewayCacheService cacheService;

    public Webhook(final GatewayCacheService cacheService, final long botId, final WebhookData data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public GatewayCacheService getGatewayCacheService() {
        return cacheService;
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
        return cacheService.getChannel(getBotId(), getGuildId(), getData().getChannelId());
    }

}
