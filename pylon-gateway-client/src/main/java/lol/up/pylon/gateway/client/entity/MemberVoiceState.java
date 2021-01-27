package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.VoiceStateData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

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

    public GrpcRequest<Channel> getChannel() {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getData().getChannelId().getValue());
    }

    public Member getMember() {
        return new Member(getClient(), getBotId(), getData().getMember());
    }
}
