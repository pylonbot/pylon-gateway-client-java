package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.ChannelData;
import bot.pylon.proto.discord.v1.model.MessageData;
import bot.pylon.proto.discord.v1.rest.CreateMessageRequest;
import bot.pylon.proto.discord.v1.rest.EditChannelPermissionsRequest;
import bot.pylon.proto.discord.v1.rest.EditMessageRequest;
import bot.pylon.proto.discord.v1.rest.ModifyChannelRequest;
import com.google.protobuf.ByteString;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.exception.InsufficientPermissionException;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Channel implements Entity<ChannelData> {

    private static final Logger log = LoggerFactory.getLogger(Channel.class);

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private ChannelData data;
    private final long userId;

    public Channel(final GatewayGrpcClient grpcClient, final long botId, final ChannelData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
        this.userId = -1;
    }

    public Channel(final GatewayGrpcClient grpcClient, final long botId, final ChannelData data, final long userId) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
        this.userId = userId;
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

    public boolean isNsfw() {
        if (getType() != ChannelData.ChannelType.GUILD_TEXT) {
            throw new IllegalArgumentException("Channel is not a guild channel");
        }
        return getData().getNsfw();
    }

    public long getUserId() {
        if (getType() != ChannelData.ChannelType.DM) {
            throw new IllegalArgumentException("Channel is not a DM channel");
        }
        return userId;
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

    public String getAsMention() {
        return "<#" + getId() + ">";
    }

    @CheckReturnValue
    public GrpcRequest<Channel> getParent() {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getParentId());
    }

    @CheckReturnValue
    public GrpcRequest<Boolean> canTalk() {
        if (getType() == ChannelData.ChannelType.DM) {
            return new FinishedRequestImpl<>(true);
        }
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId())
                .transform(member -> {
                    if (member == null) {
                        return false;
                    }
                    return canTalk(member);
                });
    }

    public GrpcRequest<User> getUser() {
        return getClient().getCacheService().getUser(getBotId(), getUserId());
    }

    public boolean canTalk(final Member member) {
        if (getType() == ChannelData.ChannelType.DM) {
            return true;
        }
        return member.hasPermission(this, Permission.VIEW_CHANNEL, Permission.SEND_MESSAGES);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> getMembers() {
        return getGuild().flatTransform(Guild::getMembers)
                .transform(members -> {
                    final List<Member> filtered = new ArrayList<>(members);
                    filtered.removeIf(member -> member.hasPermission(this, Permission.VIEW_CHANNEL));
                    return filtered;
                });
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
        if (getType() == ChannelData.ChannelType.DM) {
            throw new IllegalArgumentException("Cannot delete a DM channel");
        }
        return getClient().getRestService().deleteChannel(getBotId(), getGuildId(), getData().getId(), reason);
    }

    @CheckReturnValue
    public GrpcRequest<Void> deleteMessages(final List<Long> messageIds) {
        return deleteMessages(messageIds, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> deleteMessages(final List<Long> messageIds, @Nullable final String reason) {
        return getClient().getRestService().bulkDeleteMessages(getBotId(), getGuildId(), getId(), messageIds, reason);
    }

    @CheckReturnValue
    public GrpcRequest<Void> deleteMessageById(final long messageId) {
        return deleteMessageById(messageId, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> deleteMessageById(final long messageId, @Nullable final String reason) {
        return getClient().getRestService().deleteMessage(getBotId(), getGuildId(), getId(), messageId, reason);
    }

    @CheckReturnValue
    public GrpcRequest<Message> createMessage(final Consumer<CreateMessageRequest.Builder> consumer) {
        final CreateMessageRequest.Builder builder = CreateMessageRequest.newBuilder();
        consumer.accept(builder);
        builder.setChannelId(getData().getId());
        if (builder.hasEmbed()) {
            final MessageData.MessageEmbedData.Builder embedBuilder = builder.getEmbedBuilder();
            if (embedBuilder.getColor() < 0) {
                log.warn("Received negative color value, applying 0xFFFFFF flag", new RuntimeException("Invalid " +
                        "color"));
                embedBuilder.setColor(embedBuilder.getColor() & 0xFFFFFF);
            }
        }
        if(getGuildId() > 0) {
            final Member member = getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId())
                    .complete(); // blocking on purpose
            if (!canTalk(member)) {
                throw new InsufficientPermissionException(Permission.VIEW_CHANNEL, Permission.SEND_MESSAGES);
            }
            if(builder.hasEmbed()) {
                if (!member.hasPermission(this, Permission.EMBED_LINKS)) {
                    throw new InsufficientPermissionException(Permission.EMBED_LINKS);
                }
            }
            if(builder.hasAttachment()) {
                if (!member.hasPermission(this, Permission.ATTACH_FILES)) {
                    throw new InsufficientPermissionException(Permission.ATTACH_FILES);
                }
            }
        }
        return getClient().getRestService().createMessage(getBotId(), getGuildId(), builder.build());
    }

    @CheckReturnValue
    public GrpcRequest<Message> createMessage(final String text) {
        return createMessage(builder -> builder.setContent(text));
    }

    @CheckReturnValue
    public GrpcRequest<Message> createMessage(final MessageData.MessageEmbedData embedData) {
        return createMessage(builder -> builder.setEmbed(embedData));
    }

    @CheckReturnValue
    public GrpcRequest<Message> sendFile(final byte[] data, final String fileName) {
        return createMessage(builder -> builder.setAttachment(CreateMessageRequest.Attachment.newBuilder()
                .setContent(ByteString.copyFrom(data))
                .setName(fileName)
                .build()));
    }

    @CheckReturnValue
    public GrpcRequest<Message> editMessageById(final long messageId, Consumer<EditMessageRequest.Builder> consumer) {
        final EditMessageRequest.Builder builder = EditMessageRequest.newBuilder()
                .setMessageId(messageId)
                .setChannelId(getId());
        consumer.accept(builder);
        return getClient().getRestService().editMessage(getBotId(), getGuildId(), builder.build());
    }

    @CheckReturnValue
    public GrpcRequest<Message> editMessageById(final long messageId, final String content) {
        return editMessageById(messageId, builder -> builder.setContent(content));
    }

    @CheckReturnValue
    public GrpcRequest<Message> editMessageById(final long messageId, final MessageData.MessageEmbedData embedData) {
        return editMessageById(messageId, builder -> builder.setEmbed(embedData));
    }

    @CheckReturnValue
    public GrpcRequest<Message> getMessageById(long messageId) {
        return getClient().getRestService().getMessage(getBotId(), getGuildId(), getId(), messageId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Message>> getMessages(final long before, final int limit) {
        if (before <= 0) {
            return getClient().getRestService().getMessages(getBotId(), getGuildId(), getId(), limit);
        }
        return getClient().getRestService().getMessagesBefore(getBotId(), getGuildId(), getId(), before, limit);
    }

    @CheckReturnValue
    public GrpcRequest<Void> connectVoice() {
        return connectVoice(false, false);
    }

    @CheckReturnValue
    public GrpcRequest<Void> connectVoice(final boolean mute, final boolean deaf) {
        if (getType() != ChannelData.ChannelType.GUILD_VOICE) {
            throw new IllegalArgumentException("Not a voice channel!");
        }
        return getClient().getGatewayService().updateVoiceState(getBotId(), getGuildId(), getId(), mute, deaf);
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<List<MemberVoiceState>> getVoiceStates() {
        return getClient().getCacheService().listChannelVoiceStates(getBotId(), getGuildId(), getData().getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Channel channel = (Channel) o;
        if (getType() != channel.getType()) {
            return false;
        }
        if (channel.getType() == ChannelData.ChannelType.DM) {
            return getBotId() == channel.getBotId() &&
                    getUserId() == channel.getUserId() &&
                    getId() == channel.getId();
        } else {
            return getBotId() == channel.getBotId() &&
                    channel.getGuildId() == channel.getGuildId() &&
                    getId() == channel.getId();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBotId(), getId(), getType());
    }

    @Override
    public String toString() {
        if(getType() == ChannelData.ChannelType.DM) {
            return "Channel{" +
                    "botId=" + getBotId() +
                    ", id=" + getId() +
                    ", channelType=" + getType() +
                    ", userId=" + getUserId() +
                    ", name=" + getName() +
                    '}';
        } else {
            return "Channel{" +
                    "botId=" + getBotId() +
                    ", id=" + getId() +
                    ", channelType=" + getType() +
                    ", guildId=" + getGuildId() +
                    ", name=" + getName() +
                    '}';
        }
    }
}
