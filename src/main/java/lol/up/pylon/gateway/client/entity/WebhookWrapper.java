package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.Webhook;

public class WebhookWrapper implements WrappedEntity<Webhook> {

    private final long botId;
    private final Webhook data;
    private final GatewayCacheService cacheService;

    public WebhookWrapper(final GatewayCacheService cacheService, final long botId, final Webhook data) {
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
    public Webhook getData() {
        return data;
    }

    public ChannelWrapper getChannel() {
        return cacheService.getChannel(getBotId(), getGuildId(), getData().getChannelId());
    }

}
