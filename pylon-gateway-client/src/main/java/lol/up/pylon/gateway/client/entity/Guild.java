package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.GuildData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;

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
    public CacheService getGatewayCacheService() {
        return grpcClient.getCacheService();
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return data.getId();
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
        return getGatewayCacheService().getMember(botId, getGuildId(), userId);
    }

    public Channel getChannelById(final long channelId) {
        return getGatewayCacheService().getChannel(getBotId(), getGuildId(), channelId);
    }

    public Role getRoleById(final long roleId) {
        return getGatewayCacheService().getRole(getBotId(), getGuildId(), roleId);
    }

    public Member getMemberById(final long memberId) {
        return getGatewayCacheService().getMember(getBotId(), getGuildId(), memberId);
    }

    public Emoji getEmojiById(final long emojiId) {
        return getGatewayCacheService().getEmoji(getBotId(), getGuildId(), emojiId);
    }

    public List<Channel> listChannels() {
        return getGatewayCacheService().listGuildChannels(getBotId(), getGuildId());
    }

    public List<Role> listRoles() {
        return getGatewayCacheService().listGuildRoles(getBotId(), getGuildId());
    }

    public List<Member> listMembers() {
        return getGatewayCacheService().listGuildMembers(getBotId(), getGuildId());
    }

    public List<Emoji> listEmojis() {
        return getGatewayCacheService().listGuildEmojis(getBotId(), getGuildId());
    }
}
