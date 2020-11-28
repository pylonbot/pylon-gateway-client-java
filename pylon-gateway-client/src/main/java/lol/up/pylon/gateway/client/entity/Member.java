package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.CacheService;
import bot.pylon.proto.discord.v1.model.MemberData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Member implements Entity<MemberData> {

    private final long botId;
    private final MemberData data;
    private final CacheService cacheService;

    public Member(final CacheService cacheService, final long botId, final MemberData data) {
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
        return data.getGuildId();
    }

    @Override
    public MemberData getData() {
        return data;
    }

    public List<Role> getRoles() {
        final List<Role> roles = cacheService.listGuildRoles(getBotId(), getGuildId());
        final Map<Long, Role> roleMap = new HashMap<>();
        roles.forEach(role -> roleMap.put(role.getData().getId(), role));
        return data.getRolesList().stream()
                .map(roleMap::get)
                .collect(Collectors.toList());
    }

    public User getUser() {
        return new User(cacheService, getBotId(), getData().getUser());
    }
}
