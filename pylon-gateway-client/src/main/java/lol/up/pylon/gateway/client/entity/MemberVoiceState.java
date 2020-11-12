package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.VoiceStateData;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

public class MemberVoiceState implements Entity<VoiceStateData> {

    private final long botId;
    private final VoiceStateData data;
    private final GatewayCacheService cacheService;

    public MemberVoiceState(final GatewayCacheService cacheService, final long botId, final VoiceStateData data) {
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
    public VoiceStateData getData() {
        return data;
    }

    public Channel getChannel() {
        return cacheService.getChannel(getBotId(), getGuildId(), getData().getChannelId().getValue());
    }

    public Member getMember() {
        return new Member(cacheService, getBotId(), getData().getMember());
    }
}
