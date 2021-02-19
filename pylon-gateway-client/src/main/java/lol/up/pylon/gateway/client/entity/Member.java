package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MemberData;
import bot.pylon.proto.discord.v1.rest.ModifyGuildMemberRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.exception.HierarchyException;
import lol.up.pylon.gateway.client.exception.InsufficientPermissionException;
import lol.up.pylon.gateway.client.service.request.GrpcApiRequest;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.util.PermissionUtil;
import lol.up.pylon.gateway.client.util.TimeUtil;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.*;
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

    @CheckReturnValue
    public GrpcRequest<Boolean> isOwner() {
        return getGuild().transform(guild -> guild.getOwnerId() == getId());
    }

    public boolean isOwner(final Guild guild) {
        if (guild.getId() != getGuildId()) {
            throw new IllegalArgumentException("Given guild is not the members guild");
        }
        return guild.getOwnerId() == getId();
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

    public long getTimeJoined() {
        return TimeUtil.timestampToLong(getData().getJoinedAt());
    }

    // DATA UTILITY

    public String getEffectiveName() {
        final String nickname = getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            return nickname;
        }
        return getUser().getName();
    }

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

    public boolean canInteract(final Member member) {
        return PermissionUtil.canInteract(this, member);
    }

    public boolean canInteract(final Role role) {
        return PermissionUtil.canInteract(this, role);
    }

    public boolean canInteract(final Emoji emoji) {
        return PermissionUtil.canInteract(this, emoji);
    }

    public GrpcRequest<Integer> getColorRaw() {
        return getRoles()
                .transform(roles -> roles.stream().max(Comparator.comparingInt(Role::getPosition)).orElse(null))
                .transform(Role::getColorRaw);
    }

    // REST

    @CheckReturnValue
    public GrpcApiRequest<Void> changeNickname(final String nickname) {
        return changeNickname(nickname, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> changeNickname(final String nickname, @Nullable final String reason) {
        return getClient().getRestService().modifyGuildMember(getBotId(), getGuildId(),
                ModifyGuildMemberRequest.newBuilder()
                        .setUserId(getUserId())
                        .setNick(nickname)
                        .setAuditLogReason(reason)
                        .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addRole(final long roleId) {
        return addRole(roleId, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addRole(final long roleId, @Nullable final String reason) {
        checkRolePerms(roleId);
        return getClient().getRestService().addMemberRole(getBotId(), getGuildId(), getUserId(), roleId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeRole(final long roleId) {
        return removeRole(roleId, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeRole(final long roleId, @Nullable final String reason) {
        checkRolePerms(roleId);
        return getClient().getRestService().removeMemberRole(getBotId(), getGuildId(), getUserId(), roleId, reason);
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<List<Role>> getRoles() {
        return getClient().getCacheService().listGuildRoles(getBotId(), getGuildId())
                .transform(roles -> {
                    final Map<Long, Role> roleMap = new HashMap<>();
                    roles.forEach(role -> roleMap.put(role.getData().getId(), role));
                    return getRoleIds().stream()
                            .map(roleMap::get)
                            .filter(Objects::nonNull) // however it is possible some members have invalid role-ids
                            .sorted(Comparator.comparingInt(Role::getPosition).reversed())
                            .collect(Collectors.toList());
                });
    }

    @CheckReturnValue
    public GrpcRequest<MemberVoiceState> getVoiceState() {
        return getClient().getCacheService().getVoiceState(getBotId(), getGuildId(), getUserId());
    }

    @CheckReturnValue
    public GrpcRequest<Presence> getPresence() {
        return getClient().getCacheService().getPresence(getBotId(), getGuildId(), getUserId());
    }

    private void checkRolePerms(long roleId) {
        final Member member = getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId()).complete();
        if (!member.hasPermission(Permission.MANAGE_ROLES)) {
            throw new InsufficientPermissionException(Permission.MANAGE_ROLES);
        }
        final Role role = getClient().getCacheService().getRole(getBotId(), getGuildId(), roleId).complete();
        if (!member.canInteract(role)) {
            throw new HierarchyException("I can't interact with " + role.toString() + "!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return getBotId() == member.getBotId() &&
                getGuildId() == member.getGuildId() &&
                getId() == member.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBotId(), getGuildId(), getId());
    }

    @Override
    public String toString() {
        return "Member{" +
                "botId=" + getBotId() +
                ", guildId=" + getGuildId() +
                ", id=" + getId() +
                ", effectiveName=" + getEffectiveName() +
                '}';
    }
}
