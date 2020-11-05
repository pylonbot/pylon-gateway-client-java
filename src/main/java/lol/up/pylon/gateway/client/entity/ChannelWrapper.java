package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.Channel;

import java.util.List;

public class ChannelWrapper implements WrappedEntity<Channel> {

    private final long botId;
    private final Channel data;
    private final GatewayCacheService cacheService;

    public ChannelWrapper(final GatewayCacheService cacheService, final long botId, final Channel data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
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
    public Channel getData() {
        return data;
    }

    public List<VoiceStateWrapper> getVoiceStates() {
        return cacheService.listChannelVoiceStates(getBotId(), getGuildId(), data.getId());
    }

    public List<WebhookWrapper> getWebhooks() {
        return cacheService.listChannelWebhooks(getBotId(), getGuildId(), data.getId());
    }
}
