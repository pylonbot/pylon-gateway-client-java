package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.GuildData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

import java.util.List;

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

    public Member getSelfMember() {
        return getMember(botId);
    }

    public Member getMember(final User user) {
        return getMember(user.getUserId());
    }

    public Member getMember(final long userId) {
        return getClient().getCacheService().getMember(botId, getGuildId(), userId);
    }

    public Channel getChannelById(final long channelId) {
        return getClient().getCacheService().getChannel(getBotId(), getGuildId(), channelId);
    }

    public Role getRoleById(final long roleId) {
        return getClient().getCacheService().getRole(getBotId(), getGuildId(), roleId);
    }

    public Member getMemberById(final long memberId) {
        return getClient().getCacheService().getMember(getBotId(), getGuildId(), memberId);
    }

    public Emoji getEmojiById(final long emojiId) {
        return getClient().getCacheService().getEmoji(getBotId(), getGuildId(), emojiId);
    }

    public List<Channel> listChannels() {
        return getClient().getCacheService().listGuildChannels(getBotId(), getGuildId());
    }

    public List<Role> listRoles() {
        return getClient().getCacheService().listGuildRoles(getBotId(), getGuildId());
    }

    public List<Member> listMembers() {
        return getClient().getCacheService().listGuildMembers(getBotId(), getGuildId());
    }

    public List<Emoji> listEmojis() {
        return getClient().getCacheService().listGuildEmojis(getBotId(), getGuildId());
    }
}
