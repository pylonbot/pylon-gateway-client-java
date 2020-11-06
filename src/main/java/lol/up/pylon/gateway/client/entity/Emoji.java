package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.EmojiData;

public class Emoji implements Entity<EmojiData> {

    private final long botId;
    private final EmojiData data;
    private final GatewayCacheService cacheService;

    public Emoji(final GatewayCacheService cacheService, final long botId, final EmojiData data) {
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
    public EmojiData getData() {
        return data;
    }

}
