package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MemberData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Member implements Entity<MemberData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final MemberData data;

    public Member(final GatewayGrpcClient grpcClient, final long botId, final MemberData data) {
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
        return data.getGuildId();
    }

    @Override
    public MemberData getData() {
        return data;
    }

    public void addRole(final long roleId) {
        addRole(roleId, null);
    }

    public void addRole(final long roleId, @Nullable final String reason) {
        grpcClient.getRestService().addMemberRole(botId, getGuildId(), getUserId(), roleId, reason);
    }

    public void removeRole(final long roleId) {
        removeRole(roleId, null);
    }

    public void removeRole(final long roleId, @Nullable final String reason) {
        grpcClient.getRestService().removeMemberRole(botId, getGuildId(), roleId, reason);
    }

    public List<Long> getRoleIds() {
        return data.getRolesList();
    }

    public List<Role> getRoles() {
        final List<Role> roles = getGatewayCacheService().listGuildRoles(getBotId(), getGuildId());
        final Map<Long, Role> roleMap = new HashMap<>();
        roles.forEach(role -> roleMap.put(role.getData().getId(), role));
        return getRoleIds().stream()
                .map(roleMap::get)
                .collect(Collectors.toList());
    }

    public User getUser() {
        return new User(grpcClient, getBotId(), data.getUser());
    }

    public long getUserId() {
        return data.getUser().getId();
    }
}
