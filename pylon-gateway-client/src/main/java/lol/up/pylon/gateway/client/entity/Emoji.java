package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.EmojiData;

public class Emoji implements Entity<EmojiData> {

    private final long botId;
    private final EmojiData data;
    private final CacheService cacheService;

    public Emoji(final CacheService cacheService, final long botId, final EmojiData data) {
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
    public EmojiData getData() {
        return data;
    }

}
