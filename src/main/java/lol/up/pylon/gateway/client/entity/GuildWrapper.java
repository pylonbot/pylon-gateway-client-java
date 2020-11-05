package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.Guild;

import java.util.List;

public class GuildWrapper implements WrappedEntity<Guild> {

    private final long botId;
    private final Guild data;
    private final GatewayCacheService cacheService;

    public GuildWrapper(final GatewayCacheService cacheService, final long botId, final Guild data) {
        this.cacheService = cacheService;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public GatewayCacheService getGatewayCacheService() {
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
    public Guild getData() {
        return data;
    }

    public ChannelWrapper getChannelById(final long channelId) {
        return cacheService.getChannel(getBotId(), getGuildId(), channelId);
    }

    public RoleWrapper getRoleById(final long roleId) {
        return cacheService.getRole(getBotId(), getGuildId(), roleId);
    }

    public MemberWrapper getMemberById(final long memberId) {
        return cacheService.getMember(getBotId(), getGuildId(), memberId);
    }

    public EmojiWrapper getEmojiById(final long emojiId) {
        return cacheService.getEmoji(getBotId(), getGuildId(), emojiId);
    }

    public List<ChannelWrapper> listChannels() {
        return cacheService.listGuildChannels(getBotId(), getGuildId());
    }

    public List<RoleWrapper> listRoles() {
        return cacheService.listGuildRoles(getBotId(), getGuildId());
    }

    public List<MemberWrapper> listMembers() {
        return cacheService.listGuildMembers(getBotId(), getGuildId());
    }

    public List<EmojiWrapper> listEmojis() {
        return cacheService.listGuildEmojis(getBotId(), getGuildId());
    }
}
