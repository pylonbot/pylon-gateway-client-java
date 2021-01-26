package lol.up.pylon.gateway.client.service;

import bot.pylon.proto.discord.v1.model.GuildBanData;
import bot.pylon.proto.discord.v1.model.SnowflakeListValue;
import bot.pylon.proto.discord.v1.rest.*;
import bot.pylon.proto.gateway.v1.service.GatewayRestGrpc;
import com.google.protobuf.ByteString;
import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.*;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.exception.GrpcGatewayApiException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class RestService {

    private static final Logger log = LoggerFactory.getLogger(RestService.class);

    private final GatewayRestGrpc.GatewayRestBlockingStub client;
    private final GatewayGrpcClient gatewayGrpcClient;

    public RestService(final GatewayGrpcClient gatewayGrpcClient,
                       final GatewayRestGrpc.GatewayRestBlockingStub client) {
        this.gatewayGrpcClient = gatewayGrpcClient;
        this.client = client.withCallCredentials(new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                final Metadata metadata = new Metadata();
                metadata.put(Constants.METADATA_BOT_ID, String.valueOf(Constants.CTX_BOT_ID.get()));
                metadata.put(Constants.METADATA_GUILD_ID, String.valueOf(Constants.CTX_GUILD_ID.get()));
                applier.apply(metadata);
            }

            @Override
            public void thisUsesUnstableApi() {
                // ok
            }
        });
    }

    private long getBotId() {
        final EventContext current = EventContext.current();
        if (current != null) {
            return current.getBotId();
        }
        log.warn("Missing event context in current thread. Did you manually create threads? Consider using AbstractEventReceiver#async instead!");
        return gatewayGrpcClient.getDefaultBotId();
    }

    private String getErrorMessage(final RestError apiError) {
        return "An error occurred during REST request: " +
                "HTTPStatus:" + apiError.getStatus() + " | " +
                "ErrorCode:" + apiError.getCode() + " | " +
                "Message:" + apiError.getMessage();
    }

    public Guild modifyGuild(final long guildId, final ModifyGuildRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuild(getBotId(), guildId, request);
    }

    public Guild modifyGuild(final long botId, final long guildId, final ModifyGuildRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildResponse response = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuild(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Guild(gatewayGrpcClient, botId, response.getData().getGuild());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Channel createChannel(final long guildId, final CreateGuildChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createChannel(getBotId(), guildId, request);
    }

    public Channel createChannel(final long botId, final long guildId, final CreateGuildChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateGuildChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void modifyChannelPositions(final long guildId, final ModifyGuildChannelPositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        modifyChannelPositions(getBotId(), guildId, request);
    }

    public void modifyChannelPositions(final long botId, final long guildId,
                                       final ModifyGuildChannelPositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildChannelPositionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildChannelPositions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public boolean addGuildMember(final long guildId, final AddGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addGuildMember(getBotId(), guildId, request);
    }

    public boolean addGuildMember(final long botId, final long guildId, final AddGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final AddGuildMemberResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.addGuildMember(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getAdded();
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void modifyGuildMember(final long guildId, final ModifyGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        modifyGuildMember(getBotId(), guildId, request);
    }

    public void modifyGuildMember(final long botId, final long guildId, final ModifyGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildMemberResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildMember(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void changeSelfNickname(final long guildId, final String nickName)
            throws GrpcRequestException, GrpcGatewayApiException {
        changeSelfNickname(getBotId(), guildId, nickName);
    }

    public void changeSelfNickname(final long botId, final long guildId, final String nickName)
            throws GrpcRequestException, GrpcGatewayApiException {
        changeSelfNickname(botId, guildId, ModifyCurrentUserNickRequest.newBuilder()
                .setNick(nickName)
                .build());
    }

    public void changeSelfNickname(final long botId, final long guildId, final ModifyCurrentUserNickRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyCurrentUserNickResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyCurrentUserNick(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void addMemberRole(final long guildId, final long memberId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        addMemberRole(guildId, memberId, roleId, null);
    }

    public void addMemberRole(final long guildId, final long memberId, final long roleId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        addMemberRole(getBotId(), guildId, memberId, roleId, reason);
    }

    public void addMemberRole(final long botId, final long guildId, final long memberId, final long roleId,
                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        addMemberRole(botId, guildId, AddGuildMemberRoleRequest.newBuilder()
                .setUserId(memberId)
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    public void addMemberRole(final long botId, final long guildId, final AddGuildMemberRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final AddGuildMemberRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.addGuildMemberRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void removeMemberRole(final long guildId, final long memberId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        removeMemberRole(guildId, memberId, roleId, null);
    }

    public void removeMemberRole(final long guildId, final long memberId, final long roleId,
                                 @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        removeMemberRole(getBotId(), guildId, memberId, roleId, reason);
    }

    public void removeMemberRole(final long botId, final long guildId, final long memberId, final long roleId,
                                 @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        removeMemberRole(botId, guildId, RemoveGuildMemberRoleRequest.newBuilder()
                .setUserId(memberId)
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    public void removeMemberRole(final long botId, final long guildId, final RemoveGuildMemberRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final RemoveGuildMemberRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.removeGuildMemberRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void removeGuildMember(final long botId, final long guildId, final RemoveGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final RemoveGuildMemberResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.removeGuildMember(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<GuildBanData> getGuildBans(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBans(getBotId(), guildId);
    }

    public List<GuildBanData> getGuildBans(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetGuildBansResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildBans(GetGuildBansRequest.newBuilder().build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getBansList();
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @Nullable
    public GuildBanData getGuildBan(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBan(getBotId(), guildId, userId);
    }

    @Nullable
    public GuildBanData getGuildBan(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBan(botId, guildId, GetGuildBanRequest.newBuilder()
                .setUserId(userId)
                .build());
    }

    @Nullable
    public GuildBanData getGuildBan(final long botId, final long guildId, final GetGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            if (!response.hasData() || !response.getData().hasBan()) {
                return null;
            }
            return response.getData().getBan();
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void createGuildBan(final long guildId, final long userId, final int deleteDays)
            throws GrpcRequestException, GrpcGatewayApiException {
        createGuildBan(guildId, userId, deleteDays, null);
    }

    public void createGuildBan(final long guildId, final long userId, final int deleteDays,
                               @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        createGuildBan(getBotId(), guildId, userId, deleteDays, reason);
    }

    public void createGuildBan(final long botId, final long guildId, final long userId, final int deleteDays,
                               @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        createGuildBan(botId, guildId, CreateGuildBanRequest.newBuilder()
                .setUserId(userId)
                .setDeleteMessageDays(deleteDays)
                .setAuditLogReason(reason)
                .build());
    }

    public void createGuildBan(final long botId, final long guildId, final CreateGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void removeGuildBan(final long botId, final long guildId, final RemoveGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final RemoveGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.removeGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Role createGuildRole(final long guildId, final CreateGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildRole(getBotId(), guildId, request);
    }

    public Role createGuildRole(final long botId, final long guildId, final CreateGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateGuildRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Role(gatewayGrpcClient, botId, response.getData().getRole());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<Role> modifyGuildRolePositions(final long guildId, final ModifyGuildRolePositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildRolePositions(getBotId(), guildId, request);
    }

    public List<Role> modifyGuildRolePositions(final long botId, final long guildId,
                                               final ModifyGuildRolePositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildRolePositionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildRolePositions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getRolesList().stream()
                    .map(roleData -> new Role(gatewayGrpcClient, botId, roleData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Role modifyGuildRole(final long guildId, final ModifyGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildRole(getBotId(), guildId, request);
    }

    public Role modifyGuildRole(final long botId, final long guildId, final ModifyGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Role(gatewayGrpcClient, botId, response.getData().getRole());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteGuildRole(final long guildId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteGuildRole(guildId, roleId, (String) null);
    }

    public void deleteGuildRole(final long guildId, final long roleId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteGuildRole(getBotId(), guildId, roleId, reason);
    }

    public void deleteGuildRole(final long botId, final long guildId, final long roleId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteGuildRole(botId, guildId, DeleteGuildRoleRequest.newBuilder()
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    public void deleteGuildRole(final long botId, final long guildId, final DeleteGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteGuildRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteGuildRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public int getGuildPruneCount(final long guildId, final int days, final List<Long> roles)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildPruneCount(getBotId(), guildId, days, roles);
    }

    public int getGuildPruneCount(final long botId, final long guildId, final int days, final List<Long> roles)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildPruneCount(botId, guildId, GetGuildPruneCountRequest.newBuilder()
                .setDays(days)
                .setIncludeRoles(SnowflakeListValue.newBuilder()
                        .addAllValues(roles)
                        .build())
                .build());
    }

    public int getGuildPruneCount(final long botId, final long guildId, final GetGuildPruneCountRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetGuildPruneCountResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildPruneCount(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getSerializedSize(); // todo ehhhhh?
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void beginGuildPrune(final long guildId, final BeginGuildPruneRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        beginGuildPrune(getBotId(), guildId, request);
    }

    public void beginGuildPrune(final long botId, final long guildId, final BeginGuildPruneRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final BeginGuildPruneResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.beginGuildPrune(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<String> getGuildVoiceRegions(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildVoiceRegions(getBotId(), guildId);
    }

    public List<String> getGuildVoiceRegions(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetGuildVoiceRegionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildVoiceRegions(GetGuildVoiceRegionsRequest.newBuilder().build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getRegionsList().asByteStringList().stream()
                    .map(ByteString::toStringUtf8)
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<GuildInvite> getGuildInvites(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildInvites(getBotId(), guildId);
    }

    public List<GuildInvite> getGuildInvites(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetGuildInvitesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildInvites(GetGuildInvitesRequest.newBuilder().build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getInvitesList().stream()
                    .map(inviteData -> new GuildInvite(gatewayGrpcClient, botId, inviteData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Channel modifyChannel(final long guildId, final ModifyChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyChannel(getBotId(), guildId, request);
    }

    public Channel modifyChannel(final long botId, final long guildId, final ModifyChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteChannel(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteChannel(guildId, channelId, (String) null);
    }

    public void deleteChannel(final long guildId, final long channelId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteChannel(getBotId(), guildId, channelId, reason);
    }

    public void deleteChannel(final long botId, final long guildId, final long channelId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteChannel(botId, guildId, DeleteChannelRequest.newBuilder()
                .setChannelId(channelId)
                .setAuditLogReason(reason)
                .build());
    }

    public void deleteChannel(final long botId, final long guildId, final DeleteChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Message createMessage(final long guildId, final CreateMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createMessage(getBotId(), guildId, request);
    }

    public Message createMessage(final long botId, final long guildId, final CreateMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Message crosspostMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return crosspostMessage(getBotId(), guildId, channelId, messageId);
    }

    public Message crosspostMessage(final long botId, final long guildId, final long channelId,
                                    final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return crosspostMessage(botId, guildId, CrosspostMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .build());
    }

    public Message crosspostMessage(final long botId, final long guildId, final CrosspostMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CrosspostMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.crosspostMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void createReaction(final long guildId, final long channelId, final long messageId, final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        createReaction(getBotId(), guildId, channelId, messageId, emoji);
    }

    public void createReaction(final long botId, final long guildId, final long channelId, final long messageId,
                               final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        createReaction(botId, guildId, CreateReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    public void createReaction(final long botId, final long guildId, final CreateReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateReactionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createReaction(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteOwnReaction(final long guildId, final long channelId, final long messageId, final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteOwnReaction(getBotId(), guildId, channelId, messageId, emoji);
    }

    public void deleteOwnReaction(final long botId, final long guildId, final long channelId, final long messageId,
                                  final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteOwnReaction(botId, guildId, DeleteOwnReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    public void deleteOwnReaction(final long botId, final long guildId, final DeleteOwnReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteOwnReactionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteOwnReaction(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteReaction(final long guildId, final long channelId, final long messageId, final long userId,
                               final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteReaction(getBotId(), guildId, channelId, messageId, userId, emoji);
    }

    public void deleteReaction(final long botId, final long guildId, final long channelId, final long messageId,
                               final long userId, final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteReaction(botId, guildId, DeleteUserReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setUserId(userId)
                .setEmoji(emoji)
                .build());
    }

    public void deleteReaction(final long botId, final long guildId, final DeleteUserReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteUserReactionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteUserReaction(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteAllReactions(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteAllReactions(getBotId(), guildId, channelId, messageId);
    }

    public void deleteAllReactions(final long botId, final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteAllReactions(botId, guildId, DeleteAllReactionsRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .build());
    }

    public void deleteAllReactions(final long botId, final long guildId, final DeleteAllReactionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteAllReactionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteAllReactions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteEmoteReactions(final long guildId, final long channelId, final long messageId, final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteEmoteReactions(getBotId(), guildId, channelId, messageId, emoji);
    }

    public void deleteEmoteReactions(final long botId, final long guildId, final long channelId, final long messageId,
                                     final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteEmoteReactions(botId, guildId, DeleteAllReactionsForEmojiRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    public void deleteEmoteReactions(final long botId, final long guildId,
                                     final DeleteAllReactionsForEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteAllReactionsForEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteAllReactionsForEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Message editMessage(final long guildId, final EditMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return editMessage(getBotId(), guildId, request);
    }

    public Message editMessage(final long botId, final long guildId, final EditMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final EditMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.editMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteMessage(guildId, channelId, messageId, null);
    }

    public void deleteMessage(final long guildId, final long channelId, final long messageId,
                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteMessage(getBotId(), guildId, channelId, messageId, reason);
    }

    public void deleteMessage(final long botId, final long guildId, final long channelId, final long messageId,
                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteMessage(botId, guildId, DeleteMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    public void deleteMessage(final long botId, final long guildId, final DeleteMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void bulkDeleteMessages(final long guildId, final long channelId, final List<Long> messageIds)
            throws GrpcRequestException, GrpcGatewayApiException {
        bulkDeleteMessages(guildId, channelId, messageIds, null);
    }

    public void bulkDeleteMessages(final long guildId, final long channelId, final List<Long> messageIds,
                                   @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        bulkDeleteMessages(getBotId(), guildId, channelId, messageIds, reason);
    }

    public void bulkDeleteMessages(final long botId, final long guildId, final long channelId,
                                   final List<Long> messageIds, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        bulkDeleteMessages(botId, guildId, BulkDeleteMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .addAllMessageIds(messageIds)
                .setAuditLogReason(reason)
                .build());
    }

    public void bulkDeleteMessages(final long botId, final long guildId, final BulkDeleteMessagesRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final BulkDeleteMessagesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.bulkDeleteMessages(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void editChannelPermissions(final long guildId, final EditChannelPermissionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        editChannelPermissions(getBotId(), guildId, request);
    }

    public void editChannelPermissions(final long botId, final long guildId,
                                       final EditChannelPermissionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final EditChannelPermissionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.editChannelPermissions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<GuildInvite> getChannelInvites(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getChannelInvites(getBotId(), guildId, channelId);
    }

    public List<GuildInvite> getChannelInvites(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetChannelInvitesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getChannelInvites(GetChannelInvitesRequest.newBuilder()
                            .setChannelId(channelId)
                            .build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getInvitesList().stream()
                    .map(inviteData -> new GuildInvite(gatewayGrpcClient, botId, inviteData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GuildInvite createChannelInvite(final long guildId, final CreateChannelInviteRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createChannelInvite(getBotId(), guildId, request);
    }

    public GuildInvite createChannelInvite(final long botId, final long guildId,
                                           final CreateChannelInviteRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateChannelInviteResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createChannelInvite(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new GuildInvite(gatewayGrpcClient, botId, response.getData().getInvite());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteChannelPermission(final long guildId, final DeleteChannelPermissionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteChannelPermission(getBotId(), guildId, request);
    }

    public void deleteChannelPermission(final long botId, final long guildId,
                                        final DeleteChannelPermissionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteChannelPermissionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteChannelPermission(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public long followNewsChannel(final long guildId, final long channelId, final long webhookId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return followNewsChannel(getBotId(), guildId, channelId, webhookId);
    }

    public long followNewsChannel(final long botId, final long guildId, final long channelId, final long webhookId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return followNewsChannel(botId, guildId, FollowNewsChannelRequest.newBuilder()
                .setChannelId(channelId)
                .setWebhookChannelId(webhookId)
                .build());
    }

    public long followNewsChannel(final long botId, final long guildId, final FollowNewsChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final FollowNewsChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.followNewsChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getChannelId();
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void startTyping(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        startTyping(getBotId(), guildId, channelId);
    }

    public void startTyping(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final TriggerTypingIndicatorResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.triggerTypingIndicator(TriggerTypingIndicatorRequest.newBuilder()
                            .setChannelId(channelId)
                            .build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<Message> getPinnedMessages(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getPinnedMessages(getBotId(), guildId, channelId);
    }

    public List<Message> getPinnedMessages(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetPinnedMessagesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getPinnedMessages(GetPinnedMessagesRequest.newBuilder()
                            .setChannelId(channelId)
                            .build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getMessagesList()
                    .stream()
                    .map(messageData -> new Message(gatewayGrpcClient, botId, messageData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void pinMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        pinMessage(getBotId(), channelId, messageId, null);
    }

    public void pinMessage(final long guildId, final long channelId, final long messageId,
                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        pinMessage(getBotId(), channelId, messageId, reason);
    }

    public void pinMessage(final long botId, final long guildId, final long channelId, final long messageId,
                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        pinMessage(botId, guildId, AddPinnedChannelMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    public void pinMessage(final long botId, final long guildId, final AddPinnedChannelMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final AddPinnedChannelMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.addPinnedChannelMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void unpinMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        unpinMessage(guildId, channelId, messageId);
    }

    public void unpinMessage(final long guildId, final long channelId, final long messageId,
                             @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        unpinMessage(getBotId(), guildId, channelId, messageId, reason);
    }

    public void unpinMessage(final long botId, final long guildId, final long channelId, final long messageId,
                             @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        unpinMessage(botId, guildId, DeletePinnedChannelMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    public void unpinMessage(final long botId, final long guildId, final DeletePinnedChannelMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeletePinnedChannelMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deletePinnedChannelMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public List<Emoji> listGuildEmojis(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return listGuildEmojis(getBotId(), guildId);
    }

    public List<Emoji> listGuildEmojis(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListGuildEmojisResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.listGuildEmojis(ListGuildEmojisRequest.newBuilder().build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getEmojisList().stream()
                    .map(emojiData -> new Emoji(gatewayGrpcClient, botId, emojiData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @Nullable
    public Emoji getGuildEmoji(final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildEmoji(getBotId(), guildId, emoteId);
    }

    @Nullable
    public Emoji getGuildEmoji(final long botId, final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                            .setEmojiId(emoteId)
                            .build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Emoji createGuildEmoji(final long guildId, final CreateGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildEmoji(getBotId(), guildId, request);
    }

    public Emoji createGuildEmoji(final long botId, final long guildId, final CreateGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Emoji modifyGuildEmoji(final long guildId, final long emoteId, final String name)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(guildId, emoteId, name, null);
    }

    public Emoji modifyGuildEmoji(final long guildId, final long emoteId, final String name,
                                  @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(getBotId(), guildId, emoteId, name, reason);
    }

    public Emoji modifyGuildEmoji(final long botId, final long guildId, final long emoteId, final String name,
                                  @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(botId, guildId, ModifyGuildEmojiRequest.newBuilder()
                .setEmojiId(emoteId)
                .setName(name)
                .setAuditLogReason(reason)
                .build());
    }

    public Emoji modifyGuildEmoji(final long botId, final long guildId, final ModifyGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void deleteGuildEmoji(final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteGuildEmoji(guildId, emoteId, (String) null);
    }

    public void deleteGuildEmoji(final long guildId, final long emoteId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteGuildEmoji(getBotId(), guildId, emoteId, reason);
    }

    public void deleteGuildEmoji(final long botId, final long guildId, final long emoteId,
                                 @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        deleteGuildEmoji(botId, guildId, DeleteGuildEmojiRequest.newBuilder()
                .setEmojiId(emoteId)
                .setAuditLogReason(reason)
                .build());
    }

    public void deleteGuildEmoji(final long botId, final long guildId, final DeleteGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final DeleteGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public User getSelfUser(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getSelfUser(getBotId(), guildId);
    }

    public User getSelfUser(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetCurrentUserResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getCurrentUser(GetCurrentUserRequest.newBuilder().build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new User(gatewayGrpcClient, botId, response.getData().getUser());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @Nullable
    public User getUser(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getUser(getBotId(), guildId, userId);
    }

    @Nullable
    public User getUser(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final GetUserResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getUser(GetUserRequest.newBuilder()
                            .setUserId(userId)
                            .build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new User(gatewayGrpcClient, botId, response.getData().getUser());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public User modifySelfUser(final long guildId, final ModifyCurrentUserRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifySelfUser(getBotId(), guildId, request);
    }

    public User modifySelfUser(final long botId, final long guildId, final ModifyCurrentUserRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyCurrentUserResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyCurrentUser(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new User(gatewayGrpcClient, botId, response.getData().getUser());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public void leaveGuild(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        leaveGuild(getBotId(), guildId);
    }

    public void leaveGuild(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final LeaveGuildResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.leaveGuild(LeaveGuildRequest.newBuilder().build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public Channel createDmChannel(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createDmChannel(getBotId(), guildId, userId);
    }

    public Channel createDmChannel(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final CreateDmResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createDm(CreateDmRequest.newBuilder()
                            .setRecipientId(userId)
                            .build()));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

}
