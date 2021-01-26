package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MemberData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.util.PermissionUtil;

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
    public GatewayGrpcClient getClient() {
        return grpcClient;
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return getData().getGuildId();
    }

    @Override
    public MemberData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public String getNickname() {
        return getData().getNick().getValue();
    }

    public boolean isOwner() {
        return getGuild().getOwnerId() == getId();
    }

    public List<Long> getRoleIds() {
        return getData().getRolesList();
    }

    public User getUser() {
        return new User(getClient(), getBotId(), getData().getUser());
    }

    public long getUserId() {
        return getData().getUser().getId();
    }

    public String getEffectiveName() {
        final String nickname = getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            return nickname;
        }
        return getUser().getName();
    }

    // DATA UTILITY

    public boolean hasPermission(final Permission... permissions) {
        return hasPermission(null, permissions);
    }

    public boolean hasPermission(@Nullable final Channel channel, final Permission... permissions) {
        if (channel == null) {
            return PermissionUtil.checkPermission(this, permissions);
        } else {
            return PermissionUtil.checkPermission(channel, this, permissions);
        }
    }

    // REST

    public void changeNickname(final String nickname) {
        changeNickname(nickname, null);
    }

    public void changeNickname(final String nickname, @Nullable final String reason) {
        // TODO: Implementation missing
    }

    public void addRole(final long roleId) {
        addRole(roleId, null);
    }

    public void addRole(final long roleId, @Nullable final String reason) {
        getClient().getRestService().addMemberRole(botId, getGuildId(), getUserId(), roleId, reason);
    }

    public void removeRole(final long roleId) {
        removeRole(roleId, null);
    }

    public void removeRole(final long roleId, @Nullable final String reason) {
        getClient().getRestService().removeMemberRole(botId, getGuildId(), roleId, reason);
    }

    // CACHE

    public List<Role> getRoles() {
        final List<Role> roles = getClient().getCacheService().listGuildRoles(getBotId(), getGuildId());
        final Map<Long, Role> roleMap = new HashMap<>();
        roles.forEach(role -> roleMap.put(role.getData().getId(), role));
        return getRoleIds().stream()
                .map(roleMap::get)
                .collect(Collectors.toList());
    }

}
