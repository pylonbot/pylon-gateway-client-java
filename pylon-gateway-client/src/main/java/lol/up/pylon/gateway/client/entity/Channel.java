package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.ChannelData;
import bot.pylon.proto.discord.v1.rest.CreateMessageRequest;
import bot.pylon.proto.discord.v1.rest.EditChannelPermissionsRequest;
import bot.pylon.proto.discord.v1.rest.ModifyChannelRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

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
    public GatewayGrpcClient getClient() {
        return grpcClient;
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return getData().getGuildId().getValue();
    }

    @Override
    public ChannelData getData() {
        return data;
    }

    public int getPosition() {
        return getData().getPosition();
    }

    public List<MemberVoiceState> getVoiceStates() {
        return getClient().getCacheService().listChannelVoiceStates(getBotId(), getGuildId(), getData().getId());
    }

    public void edit(final Consumer<ModifyChannelRequest.Builder> consumer) {
        final ModifyChannelRequest.Builder builder = ModifyChannelRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        this.data = grpcClient.getRestService().modifyChannel(getBotId(), getGuildId(), builder.build()).getData();
    }

    public void editPermissions(final Consumer<EditChannelPermissionsRequest.Builder> consumer) {
        final EditChannelPermissionsRequest.Builder builder = EditChannelPermissionsRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        getClient().getRestService().editChannelPermissions(getBotId(), getGuildId(), builder.build());
    }

    public void delete() {
        delete(null);
    }

    public void delete(@Nullable final String reason) {
        getClient().getRestService().deleteChannel(getBotId(), getGuildId(), getData().getId(), reason);
    }

    public Message createMessage(final Consumer<CreateMessageRequest.Builder> consumer) {
        final CreateMessageRequest.Builder builder = CreateMessageRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        return getClient().getRestService().createMessage(getBotId(), getGuildId(), builder.build());
    }

    public Message getMessageById(long messageId) {
        //return grpcClient.getRestService().message
        return null; // TODO
    }
}
