package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.GuildData;

import java.util.List;

public class Guild implements Entity<GuildData> {

    private final long botId;
    private final GuildData data;
    private final CacheService cacheService;

    public Guild(final CacheService cacheService, final long botId, final GuildData data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public CacheService getGatewayCacheService() {
        return cacheService;
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

    public Channel getChannelById(final long channelId) {
        return cacheService.getChannel(getBotId(), getGuildId(), channelId);
    }

    public Role getRoleById(final long roleId) {
        return cacheService.getRole(getBotId(), getGuildId(), roleId);
    }

    public Member getMemberById(final long memberId) {
        return cacheService.getMember(getBotId(), getGuildId(), memberId);
    }

    public Emoji getEmojiById(final long emojiId) {
        return cacheService.getEmoji(getBotId(), getGuildId(), emojiId);
    }

    public List<Channel> listChannels() {
        return cacheService.listGuildChannels(getBotId(), getGuildId());
    }

    public List<Role> listRoles() {
        return cacheService.listGuildRoles(getBotId(), getGuildId());
    }

    public List<Member> listMembers() {
        return cacheService.listGuildMembers(getBotId(), getGuildId());
    }

    public List<Emoji> listEmojis() {
        return cacheService.listGuildEmojis(getBotId(), getGuildId());
    }
}
