package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.cache.FindGuildMembersRequest;
import bot.pylon.proto.discord.v1.model.GuildData;
import bot.pylon.proto.discord.v1.model.PresenceData;
import bot.pylon.proto.discord.v1.rest.CreateGuildChannelRequest;
import bot.pylon.proto.discord.v1.rest.RemoveGuildBanRequest;
import bot.pylon.proto.discord.v1.rest.RemoveGuildMemberRequest;
import com.google.protobuf.StringValue;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class Guild implements Entity<GuildData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final GuildData data;

    public Guild(final GatewayGrpcClient grpcClient, final long botId, final GuildData data) {
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
        return getData().getId();
    }

    @Override
    public GuildData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public String getName() {
        return getData().getName();
    }

    public String getIconId() {
        final StringValue icon = getData().getIcon();
        if (icon.isInitialized()) {
            return icon.getValue();
        }
        return null;
    }

    public long getOwnerId() {
        return getData().getOwnerId();
    }

    public String getRegionRaw() {
        return getData().getRegion();
    }

    public int getVerificationLevel() {
        return getData().getVerificationLevel();
    }

    public int getMemberCount() {
        return getData().getMemberCount();
    }

    // DATA UTIL

    @CheckReturnValue
    public GrpcRequest<Member> getSelfMember() {
        return getMember(getBotId());
    }

    public boolean isMember(final User user) {
        return isMember(user.getId());
    }

    public boolean isMember(final long memberId) {
        return getMemberById(memberId) != null;
    }

    public String getIconUrl() {
        final String icon = getIconId();
        if (icon == null) {
            return null;
        }
        return "https://cdn.discordapp.com/icon/" + getId() + "/" + icon + "." +
                (icon.startsWith("a_") ? "gif" : "png");
    }

    @CheckReturnValue
    public GrpcRequest<Member> getOwner() {
        return getMember(getOwnerId());
    }

    public long getPublicRoleId() {
        return getGuildId();
    }

    @CheckReturnValue
    public GrpcRequest<Role> getPublicRole() {
        return getRoleById(getPublicRoleId());
    }

    // REST

    @CheckReturnValue
    public GrpcRequest<Channel> createChannel(final CreateGuildChannelRequest request) {
        return getClient().getRestService().createChannel(getBotId(), getGuildId(), request);
    }

    @CheckReturnValue
    public GrpcRequest<Void> ban(final long userId) {
        return ban(userId, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> ban(final long userId, @Nullable final String reason) {
        return ban(userId, 7, reason);
    }

    @CheckReturnValue
    public GrpcRequest<Void> ban(final long userId, final int deleteDays) {
        return ban(userId, deleteDays, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> ban(final long userId, final int deleteDays, @Nullable final String reason) {
        return getClient().getRestService().createGuildBan(getBotId(), getId(), userId, deleteDays, reason);
    }

    @CheckReturnValue
    public GrpcRequest<Void> unban(final long userId) {
        return unban(userId, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> unban(final long userId, @Nullable final String reason) {
        return getClient().getRestService().removeGuildBan(getBotId(), getId(), RemoveGuildBanRequest.newBuilder()
                .setUserId(userId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcRequest<Void> kick(final long userId) {
        return kick(userId, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> kick(final long userId, @Nullable final String reason) {
        return getClient().getRestService().removeGuildMember(getBotId(), getGuildId(),
                RemoveGuildMemberRequest.newBuilder()
                        .setUserId(userId)
                        .setAuditLogReason(reason)
                        .build());
    }

    @CheckReturnValue
    public GrpcRequest<Ban> retrieveBan(final long userId) {
        return getClient().getRestService().getGuildBan(getBotId(), getGuildId(), userId);
    }

    @CheckReturnValue
    public GrpcRequest<Void> disconnectVoice() {
        return getClient().getGatewayService().updateVoiceState(getBotId(), getGuildId(), 0);
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<Member> getMember(final User user) {
        return getMember(user.getUserId());
    }

    @CheckReturnValue
    public GrpcRequest<Member> getMember(final long userId) {
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), userId);
    }

    @CheckReturnValue
    public GrpcRequest<Channel> getChannelById(final long channelId) {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), channelId);
    }

    @CheckReturnValue
    public GrpcRequest<Role> getRoleById(final long roleId) {
        return getClient().getCacheService().getRole(getBotId(), getGuildId(), roleId);
    }

    @CheckReturnValue
    public GrpcRequest<Member> getMemberById(final long memberId) {
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), memberId);
    }

    @CheckReturnValue
    public GrpcRequest<Emoji> getEmojiById(final long emojiId) {
        return getClient().getCacheService().getEmoji(getBotId(), getGuildId(), emojiId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Channel>> getChannels() {
        return getClient().getCacheService().listGuildChannels(getBotId(), getGuildId());
    }

    @CheckReturnValue
    public GrpcRequest<List<Role>> getRoles() {
        return getClient().getCacheService().listGuildRoles(getBotId(), getGuildId());
    }

    @CheckReturnValue
    public GrpcRequest<Member> findMemberByName(final String name) {
        return getClient().getCacheService().findMembers(getBotId(), getGuildId(), FindGuildMembersRequest.newBuilder()
                .setName(name)
                .setLimit(1)
                .build())
                .transform(members -> members.stream().findFirst().orElse(null));
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> findMembersByPrefix(final String prefix) {
        return getClient().getCacheService().findMembers(getBotId(), getGuildId(), FindGuildMembersRequest.newBuilder()
                .setPrefix(prefix)
                .setLimit(100)
                .build());
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> findMembersByOnlineStatus(final PresenceData.OnlineStatus status) {
        return getClient().getCacheService().findMembers(getBotId(), getGuildId(), FindGuildMembersRequest.newBuilder()
                .setStatus(status)
                .setLimit(Integer.MAX_VALUE) // todo: bad
                .build());
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> getMembers() {
        return getClient().getCacheService().listGuildMembers(getBotId(), getGuildId());
    }

    @CheckReturnValue
    public GrpcRequest<List<Emoji>> getEmojis() {
        return getClient().getCacheService().listGuildEmojis(getBotId(), getGuildId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Guild guild = (Guild) o;
        return getBotId() == guild.getBotId() &&
                getId() == guild.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBotId(), getId(), getName());
    }

    @Override
    public String toString() {
        return "Guild{" +
                "botId=" + getBotId() +
                ", id=" + getId() +
                ", name=" + getName() +
                '}';
    }
}
