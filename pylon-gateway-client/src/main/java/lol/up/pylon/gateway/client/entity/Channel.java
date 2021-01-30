package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.ChannelData;
import bot.pylon.proto.discord.v1.rest.CreateMessageRequest;
import bot.pylon.proto.discord.v1.rest.EditChannelPermissionsRequest;
import bot.pylon.proto.discord.v1.rest.ModifyChannelRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;
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

    // DATA

    public long getId() {
        return getData().getId();
    }

    public String getName() {
        return getData().getName();
    }

    public int getPosition() {
        return getData().getPosition();
    }

    public long getParentId() {
        return getData().getParentId().getValue();
    }

    public ChannelData.ChannelType getType() {
        return getData().getType();
    }

    public List<ChannelData.ChannelPermissionOverwriteData> getPermissionOverwrites() {
        return getData().getPermissionOverwritesList();
    }

    public ChannelData.ChannelPermissionOverwriteData getPermissionOverwrite(final Role role) {
        return getRolePermissionOverwrite(role.getId());
    }

    public ChannelData.ChannelPermissionOverwriteData getPermissionOverwrite(final Member member) {
        return getUserPermissionOverwrite(member.getId());
    }

    public ChannelData.ChannelPermissionOverwriteData getRolePermissionOverwrite(final long roleId) {
        return getPermissionOverwrites().stream()
                .filter(overwrite -> overwrite.getType() == ChannelData.ChannelPermissionOverwriteData.ChannelPermissionOverwriteType.ROLE)
                .filter(overwrite -> overwrite.getId() == roleId)
                .findFirst()
                .orElse(null);
    }

    public ChannelData.ChannelPermissionOverwriteData getUserPermissionOverwrite(final long userId) {
        return getPermissionOverwrites().stream()
                .filter(overwrite -> overwrite.getType() == ChannelData.ChannelPermissionOverwriteData.ChannelPermissionOverwriteType.MEMBER)
                .filter(overwrite -> overwrite.getId() == userId)
                .findFirst()
                .orElse(null);
    }

    // DATA UTIL

    @CheckReturnValue
    public GrpcRequest<Channel> getParent() {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getParentId());
    }

    @CheckReturnValue
    public GrpcRequest<Boolean> canTalk() {
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId())
                .transform(member -> {
                    if (member == null) {
                        return false;
                    }
                    return canTalk(member);
                });
    }

    public boolean canTalk(final Member member) {
        return member.hasPermission(this, Permission.VIEW_CHANNEL, Permission.SEND_MESSAGES);
    }

    // REST

    @CheckReturnValue
    public GrpcRequest<Void> edit(final Consumer<ModifyChannelRequest.Builder> consumer) {
        final ModifyChannelRequest.Builder builder = ModifyChannelRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        return grpcClient.getRestService().modifyChannel(getBotId(), getGuildId(), builder.build())
                .transform(channel -> {
                    this.data = channel.getData();
                    return null;
                });
    }

    @CheckReturnValue
    public GrpcRequest<Void> editPermissions(final Consumer<EditChannelPermissionsRequest.Builder> consumer) {
        final EditChannelPermissionsRequest.Builder builder = EditChannelPermissionsRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        return getClient().getRestService().editChannelPermissions(getBotId(), getGuildId(), builder.build());
    }

    @CheckReturnValue
    public GrpcRequest<Void> delete() {
        return delete(null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> delete(@Nullable final String reason) {
        return getClient().getRestService().deleteChannel(getBotId(), getGuildId(), getData().getId(), reason);
    }

    @CheckReturnValue
    public GrpcRequest<Message> createMessage(final Consumer<CreateMessageRequest.Builder> consumer) {
        final CreateMessageRequest.Builder builder = CreateMessageRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        return getClient().getRestService().createMessage(getBotId(), getGuildId(), builder.build());
    }

    @CheckReturnValue
    public GrpcRequest<Message> createMessage(final String text) {
        return createMessage(builder -> builder.setContent(text));
    }

    @CheckReturnValue
    public GrpcRequest<Message> getMessageById(long messageId) {
        //return grpcClient.getRestService().message
        return null; // TODO
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<List<MemberVoiceState>> getVoiceStates() {
        return getClient().getCacheService().listChannelVoiceStates(getBotId(), getGuildId(), getData().getId());
    }
}
