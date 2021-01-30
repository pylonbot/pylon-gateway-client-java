package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MemberData;
import bot.pylon.proto.discord.v1.rest.ModifyGuildMemberRequest;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.util.PermissionUtil;

import javax.annotation.CheckReturnValue;
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

    public boolean canInteract(final Member member) {
        return PermissionUtil.canInteract(this, member);
    }

    public boolean canInteract(final Role role) {
        return PermissionUtil.canInteract(this, role);
    }

    public boolean canInteract(final Emoji emoji) {
        return PermissionUtil.canInteract(this, emoji);
    }

    // REST

    @CheckReturnValue
    public GrpcRequest<Void> changeNickname(final String nickname) {
        return changeNickname(nickname, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> changeNickname(final String nickname, @Nullable final String reason) {
        return getClient().getRestService().modifyGuildMember(getBotId(), getGuildId(), ModifyGuildMemberRequest.newBuilder()
                .setUserId(getUserId())
                .setNick(nickname)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcRequest<Void> addRole(final long roleId) {
        return addRole(roleId, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> addRole(final long roleId, @Nullable final String reason) {
        return getClient().getRestService().addMemberRole(getBotId(), getGuildId(), getUserId(), roleId, reason);
    }

    @CheckReturnValue
    public GrpcRequest<Void> removeRole(final long roleId) {
        return removeRole(roleId, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> removeRole(final long roleId, @Nullable final String reason) {
        return getClient().getRestService().removeMemberRole(getBotId(), getGuildId(), roleId, reason);
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
                            .collect(Collectors.toList());
                });
    }

    @CheckReturnValue
    public GrpcRequest<MemberVoiceState> getVoiceState() {
        return getClient().getCacheService().getVoiceState(getBotId(), getGuildId(), getUserId());
    }

}
