package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.GuildData;
import bot.pylon.proto.discord.v1.rest.CreateGuildChannelRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

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

    public GrpcRequest<Member> getSelfMember() {
        return getMember(botId);
    }

    public long getOwnerId() {
        return getData().getOwnerId();
    }


    // DATA UTIL

    public boolean isMember(final User user) {
        return isMember(user.getId());
    }

    public boolean isMember(final long memberId) {
        return getMemberById(memberId) != null;
    }

    public long getPublicRoleId() {
        return getGuildId();
    }

    public GrpcRequest<Role> getPublicRole() {
        return getRoleById(getPublicRoleId());
    }

    // REST

    public GrpcRequest<Channel> createChannel(final CreateGuildChannelRequest request) {
        return getClient().getRestService().createChannel(getBotId(), getGuildId(), request);
    }

    // CACHE

    public GrpcRequest<Member> getMember(final User user) {
        return getMember(user.getUserId());
    }

    public GrpcRequest<Member> getMember(final long userId) {
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), userId);
    }

    public GrpcRequest<Channel> getChannelById(final long channelId) {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), channelId);
    }

    public GrpcRequest<Role> getRoleById(final long roleId) {
        return getClient().getCacheService().getRole(getBotId(), getGuildId(), roleId);
    }

    public GrpcRequest<Member> getMemberById(final long memberId) {
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), memberId);
    }

    public GrpcRequest<Emoji> getEmojiById(final long emojiId) {
        return getClient().getCacheService().getEmoji(getBotId(), getGuildId(), emojiId);
    }

    public GrpcRequest<List<Channel>> getChannels() {
        return getClient().getCacheService().listGuildChannels(getBotId(), getGuildId());
    }

    public GrpcRequest<List<Role>> getRoles() {
        return getClient().getCacheService().listGuildRoles(getBotId(), getGuildId());
    }

    public GrpcRequest<List<Member>> getMembers() {
        return getClient().getCacheService().listGuildMembers(getBotId(), getGuildId());
    }

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
        return getId() == guild.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(grpcClient, botId, data);
    }
}
