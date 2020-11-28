package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.WebhookData;

public class Webhook implements Entity<WebhookData> {

    private final long botId;
    private final WebhookData data;
    private final CacheService cacheService;

    public Webhook(final CacheService cacheService, final long botId, final WebhookData data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public CacheService getGatewayCacheService() {
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
