package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.VoiceStateData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

public class MemberVoiceState implements Entity<VoiceStateData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final VoiceStateData data;

    public MemberVoiceState(final GatewayGrpcClient grpcClient, final long botId, final VoiceStateData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public GatewayGrpcClient getClient() {
        return grpcClient;
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return getData().getGuildId();
    }

    @Override
    public VoiceStateData getData() {
        return data;
    }

    // DATA

    public boolean isInVoiceChannel() {
        return getData().hasChannelId();
    }

    public long getChannelId() {
        if(!isInVoiceChannel()) {
            throw new IllegalArgumentException("Not connected to a voice channel!");
        }
        return getData().getChannelId().getValue();
    }

    public boolean isDeafened() {
        return getData().getSelfDeaf();
    }

    public boolean isGuildDeafened() {
        return getData().getDeaf();
    }

    public boolean isMuted() {
        return getData().getSelfMute();
    }

    public boolean isGuildMuted() {
        return getData().getMute();
    }

    public boolean isGameStreaming() {
        return getData().getSelfStream();
    }

    public boolean isVideoStreaming() {
        return getData().getSelfVideo();
    }

    // DATA UTIL


    // REST


    // CACHE

    @CheckReturnValue
    public GrpcRequest<Channel> getChannel() {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getChannelId());
    }

    public Member getMember() {
        return new Member(getClient(), getBotId(), getData().getMember());
    }
}
