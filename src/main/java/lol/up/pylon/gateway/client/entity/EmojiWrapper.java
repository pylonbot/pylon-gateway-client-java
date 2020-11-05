package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.Emoji;

public class EmojiWrapper implements WrappedEntity<Emoji> {

    private final long botId;
    private final Emoji data;
    private final GatewayCacheService cacheService;

    public EmojiWrapper(final GatewayCacheService cacheService, final long botId, final Emoji data) {
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
    public Emoji getData() {
        return data;
    }

}
