package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.service.GatewayCacheService;
import rpc.gateway.v1.Member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemberWrapper implements WrappedEntity<Member> {

    private final long botId;
    private final Member data;
    private final GatewayCacheService cacheService;

    public MemberWrapper(final GatewayCacheService cacheService, final long botId, final Member data) {
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
        return data.getGuildId();
    }

    @Override
    public Member getData() {
        return data;
    }

    public List<RoleWrapper> getRoles() {
        final List<RoleWrapper> roles = cacheService.listGuildRoles(getBotId(), getGuildId());
        final Map<Long, RoleWrapper> roleMap = new HashMap<>();
        roles.forEach(role -> roleMap.put(role.getData().getId(), role));
        return data.getRolesList().stream()
                .map(roleMap::get)
                .collect(Collectors.toList());
    }

    public UserWrapper getUser() {
        return new UserWrapper(cacheService, getBotId(), getData().getUser());
    }
}
