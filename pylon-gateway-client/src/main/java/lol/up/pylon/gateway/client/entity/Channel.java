package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.ChannelData;
import bot.pylon.proto.discord.v1.rest.EditChannelPermissionsRequest;
import bot.pylon.proto.discord.v1.rest.ModifyChannelRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class Channel implements Entity<ChannelData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private ChannelData data;

    public Channel(final GatewayGrpcClient grpcClient, final long botId, final ChannelData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public CacheService getGatewayCacheService() {
        return grpcClient.getCacheService();
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
        return getGatewayCacheService().listChannelVoiceStates(getBotId(), getGuildId(), data.getId());
    }

    public void edit(final Consumer<ModifyChannelRequest.Builder> consumer) {
        final ModifyChannelRequest.Builder builder = ModifyChannelRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(data.getId());
        this.data = grpcClient.getRestService().modifyChannel(getBotId(), getGuildId(), builder.build()).getData();
    }

    public void editPermissions(final Consumer<EditChannelPermissionsRequest.Builder> consumer) {
        final EditChannelPermissionsRequest.Builder builder = EditChannelPermissionsRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(data.getId());
        grpcClient.getRestService().editChannelPermissions(getBotId(), getGuildId(), builder.build());
    }

    public void delete() {
        delete(null);
    }

    public void delete(@Nullable final String reason) {
        grpcClient.getRestService().deleteChannel(getBotId(), getGuildId(), data.getId(), reason);
    }
}
