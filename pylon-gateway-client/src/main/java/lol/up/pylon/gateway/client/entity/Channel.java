package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import bot.pylon.proto.discord.v1.model.ChannelData;

import java.util.List;

public class Channel implements Entity<ChannelData> {

    private final long botId;
    private final ChannelData data;
    private final GatewayCacheService cacheService;

    public Channel(final GatewayCacheService cacheService, final long botId, final ChannelData data) {
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
        return data.getGuildId().getValue();
    }

    @Override
    public ChannelData getData() {
        return data;
    }

    public List<MemberVoiceState> getVoiceStates() {
        return cacheService.listChannelVoiceStates(getBotId(), getGuildId(), data.getId());
    }
}
