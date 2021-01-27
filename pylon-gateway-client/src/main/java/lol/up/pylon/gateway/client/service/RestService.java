package lol.up.pylon.gateway.client.service;

import bot.pylon.proto.discord.v1.model.GuildBanData;
import bot.pylon.proto.discord.v1.model.SnowflakeListValue;
import bot.pylon.proto.discord.v1.rest.*;
import bot.pylon.proto.gateway.v1.service.GatewayRestGrpc;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.*;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.event.EventExecutorService;
import lol.up.pylon.gateway.client.exception.GrpcGatewayApiException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.service.request.GrpcRequestImpl;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class RestService {

    private static final Logger log = LoggerFactory.getLogger(RestService.class);

    private final GatewayRestGrpc.GatewayRestFutureStub client;
    private final GatewayGrpcClient gatewayGrpcClient;
    private final ExecutorService executorService;

    public RestService(final GatewayGrpcClient gatewayGrpcClient,
                       final GatewayRestGrpc.GatewayRestFutureStub client,
                       final ExecutorService executorService) {
        this.gatewayGrpcClient = gatewayGrpcClient;
        this.executorService = new EventExecutorService(executorService, EventContext.localContext());
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
        log.warn("Missing event context in current thread. Did you manually create threads? Consider using " +
                "AbstractEventReceiver#async instead!");
        return gatewayGrpcClient.getDefaultBotId();
    }

    private String getErrorMessage(final RestError apiError) {
        return "An error occurred during REST request: " +
                "HTTPStatus:" + apiError.getStatus() + " | " +
                "ErrorCode:" + apiError.getCode() + " | " +
                "Message:" + apiError.getMessage();
    }

    public GrpcRequest<Guild> modifyGuild(final long guildId, final ModifyGuildRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuild(getBotId(), guildId, request);
    }

    public GrpcRequest<Guild> modifyGuild(final long botId, final long guildId,
                                          final ModifyGuildRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyGuildResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId,
                            Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyGuild(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Guild(gatewayGrpcClient, botId, response.getData().getGuild());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Channel> createChannel(final long guildId, final CreateGuildChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createChannel(getBotId(), guildId, request);
    }

    public GrpcRequest<Channel> createChannel(final long botId, final long guildId,
                                              final CreateGuildChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateGuildChannelResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId,
                            Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createGuildChannel(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> modifyChannelPositions(final long guildId,
                                                    final ModifyGuildChannelPositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyChannelPositions(getBotId(), guildId, request);
    }

    public GrpcRequest<Void> modifyChannelPositions(final long botId, final long guildId,
                                                    final ModifyGuildChannelPositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyGuildChannelPositionsResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID,
                            botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyGuildChannelPositions(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Boolean> addGuildMember(final long guildId, final AddGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addGuildMember(getBotId(), guildId, request);
    }

    public GrpcRequest<Boolean> addGuildMember(final long botId, final long guildId,
                                               final AddGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<AddGuildMemberResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.addGuildMember(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getAdded();
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> modifyGuildMember(final long guildId, final ModifyGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildMember(getBotId(), guildId, request);
    }

    public GrpcRequest<Void> modifyGuildMember(final long botId, final long guildId,
                                               final ModifyGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyGuildMemberResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyGuildMember(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> changeSelfNickname(final long guildId, final String nickName)
            throws GrpcRequestException, GrpcGatewayApiException {
        return changeSelfNickname(getBotId(), guildId, nickName);
    }

    public GrpcRequest<Void> changeSelfNickname(final long botId, final long guildId, final String nickName)
            throws GrpcRequestException, GrpcGatewayApiException {
        return changeSelfNickname(botId, guildId, ModifyCurrentUserNickRequest.newBuilder()
                .setNick(nickName)
                .build());
    }

    public GrpcRequest<Void> changeSelfNickname(final long botId, final long guildId,
                                                final ModifyCurrentUserNickRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyCurrentUserNickResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyCurrentUserNick(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> addMemberRole(final long guildId, final long memberId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addMemberRole(guildId, memberId, roleId, null);
    }

    public GrpcRequest<Void> addMemberRole(final long guildId, final long memberId, final long roleId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addMemberRole(getBotId(), guildId, memberId, roleId, reason);
    }

    public GrpcRequest<Void> addMemberRole(final long botId, final long guildId, final long memberId, final long roleId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addMemberRole(botId, guildId, AddGuildMemberRoleRequest.newBuilder()
                .setUserId(memberId)
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> addMemberRole(final long botId, final long guildId,
                                           final AddGuildMemberRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<AddGuildMemberRoleResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.addGuildMemberRole(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> removeMemberRole(final long guildId, final long memberId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return removeMemberRole(guildId, memberId, roleId, null);
    }

    public GrpcRequest<Void> removeMemberRole(final long guildId, final long memberId, final long roleId,
                                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return removeMemberRole(getBotId(), guildId, memberId, roleId, reason);
    }

    public GrpcRequest<Void> removeMemberRole(final long botId, final long guildId, final long memberId,
                                              final long roleId,
                                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return removeMemberRole(botId, guildId, RemoveGuildMemberRoleRequest.newBuilder()
                .setUserId(memberId)
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> removeMemberRole(final long botId, final long guildId,
                                              final RemoveGuildMemberRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<RemoveGuildMemberRoleResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.removeGuildMemberRole(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> removeGuildMember(final long botId, final long guildId,
                                               final RemoveGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<RemoveGuildMemberResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.removeGuildMember(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<GuildBanData>> getGuildBans(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBans(getBotId(), guildId);
    }

    public GrpcRequest<List<GuildBanData>> getGuildBans(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetGuildBansResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildBans(GetGuildBansRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getBansList();
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<GuildBanData> getGuildBan(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBan(getBotId(), guildId, userId);
    }

    public GrpcRequest<GuildBanData> getGuildBan(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBan(botId, guildId, GetGuildBanRequest.newBuilder()
                .setUserId(userId)
                .build());
    }

    public GrpcRequest<GuildBanData> getGuildBan(final long botId, final long guildId, final GetGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetGuildBanResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildBan(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                if (!response.hasData() || !response.getData().hasBan()) {
                    return null;
                }
                return response.getData().getBan();
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> createGuildBan(final long guildId, final long userId, final int deleteDays)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildBan(guildId, userId, deleteDays, null);
    }

    public GrpcRequest<Void> createGuildBan(final long guildId, final long userId, final int deleteDays,
                                            @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildBan(getBotId(), guildId, userId, deleteDays, reason);
    }

    public GrpcRequest<Void> createGuildBan(final long botId, final long guildId, final long userId,
                                            final int deleteDays,
                                            @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildBan(botId, guildId, CreateGuildBanRequest.newBuilder()
                .setUserId(userId)
                .setDeleteMessageDays(deleteDays)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> createGuildBan(final long botId, final long guildId, final CreateGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateGuildBanResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createGuildBan(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> removeGuildBan(final long botId, final long guildId, final RemoveGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<RemoveGuildBanResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.removeGuildBan(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Role> createGuildRole(final long guildId, final CreateGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildRole(getBotId(), guildId, request);
    }

    public GrpcRequest<Role> createGuildRole(final long botId, final long guildId, final CreateGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateGuildRoleResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createGuildRole(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Role(gatewayGrpcClient, botId, response.getData().getRole());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<Role>> modifyGuildRolePositions(final long guildId,
                                                            final ModifyGuildRolePositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildRolePositions(getBotId(), guildId, request);
    }

    public GrpcRequest<List<Role>> modifyGuildRolePositions(final long botId, final long guildId,
                                                            final ModifyGuildRolePositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyGuildRolePositionsResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyGuildRolePositions(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getRolesList().stream()
                        .map(roleData -> new Role(gatewayGrpcClient, botId, roleData))
                        .collect(Collectors.toList());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Role> modifyGuildRole(final long guildId, final ModifyGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildRole(getBotId(), guildId, request);
    }

    public GrpcRequest<Role> modifyGuildRole(final long botId, final long guildId, final ModifyGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyGuildRoleResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyGuildRole(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Role(gatewayGrpcClient, botId, response.getData().getRole());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteGuildRole(final long guildId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildRole(guildId, roleId, (String) null);
    }

    public GrpcRequest<Void> deleteGuildRole(final long guildId, final long roleId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildRole(getBotId(), guildId, roleId, reason);
    }

    public GrpcRequest<Void> deleteGuildRole(final long botId, final long guildId, final long roleId,
                                             @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildRole(botId, guildId, DeleteGuildRoleRequest.newBuilder()
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> deleteGuildRole(final long botId, final long guildId, final DeleteGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteGuildRoleResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteGuildRole(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Integer> getGuildPruneCount(final long guildId, final int days, final List<Long> roles)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildPruneCount(getBotId(), guildId, days, roles);
    }

    public GrpcRequest<Integer> getGuildPruneCount(final long botId, final long guildId, final int days,
                                                   final List<Long> roles)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildPruneCount(botId, guildId, GetGuildPruneCountRequest.newBuilder()
                .setDays(days)
                .setIncludeRoles(SnowflakeListValue.newBuilder()
                        .addAllValues(roles)
                        .build())
                .build());
    }

    public GrpcRequest<Integer> getGuildPruneCount(final long botId, final long guildId,
                                                   final GetGuildPruneCountRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetGuildPruneCountResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildPruneCount(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getSerializedSize(); // todo ehhhhh? 
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> beginGuildPrune(final long guildId, final BeginGuildPruneRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return beginGuildPrune(getBotId(), guildId, request);
    }

    public GrpcRequest<Void> beginGuildPrune(final long botId, final long guildId, final BeginGuildPruneRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<BeginGuildPruneResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.beginGuildPrune(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<String>> getGuildVoiceRegions(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildVoiceRegions(getBotId(), guildId);
    }

    public GrpcRequest<List<String>> getGuildVoiceRegions(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetGuildVoiceRegionsResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildVoiceRegions(GetGuildVoiceRegionsRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getRegionsList().asByteStringList().stream()
                        .map(ByteString::toStringUtf8)
                        .collect(Collectors.toList());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<GuildInvite>> getGuildInvites(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildInvites(getBotId(), guildId);
    }

    public GrpcRequest<List<GuildInvite>> getGuildInvites(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetGuildInvitesResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildInvites(GetGuildInvitesRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getInvitesList().stream()
                        .map(inviteData -> new GuildInvite(gatewayGrpcClient, botId, inviteData))
                        .collect(Collectors.toList());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Channel> modifyChannel(final long guildId, final ModifyChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyChannel(getBotId(), guildId, request);
    }

    public GrpcRequest<Channel> modifyChannel(final long botId, final long guildId, final ModifyChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyChannelResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyChannel(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteChannel(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannel(guildId, channelId, (String) null);
    }

    public GrpcRequest<Void> deleteChannel(final long guildId, final long channelId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannel(getBotId(), guildId, channelId, reason);
    }

    public GrpcRequest<Void> deleteChannel(final long botId, final long guildId, final long channelId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannel(botId, guildId, DeleteChannelRequest.newBuilder()
                .setChannelId(channelId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> deleteChannel(final long botId, final long guildId, final DeleteChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteChannelResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteChannel(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Message> createMessage(final long guildId, final CreateMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createMessage(getBotId(), guildId, request);
    }

    public GrpcRequest<Message> createMessage(final long botId, final long guildId, final CreateMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateMessageResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createMessage(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Message> crosspostMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return crosspostMessage(getBotId(), guildId, channelId, messageId);
    }

    public GrpcRequest<Message> crosspostMessage(final long botId, final long guildId, final long channelId,
                                                 final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return crosspostMessage(botId, guildId, CrosspostMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .build());
    }

    public GrpcRequest<Message> crosspostMessage(final long botId, final long guildId,
                                                 final CrosspostMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CrosspostMessageResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.crosspostMessage(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> createReaction(final long guildId, final long channelId, final long messageId,
                                            final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createReaction(getBotId(), guildId, channelId, messageId, emoji);
    }

    public GrpcRequest<Void> createReaction(final long botId, final long guildId, final long channelId,
                                            final long messageId,
                                            final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createReaction(botId, guildId, CreateReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    public GrpcRequest<Void> createReaction(final long botId, final long guildId, final CreateReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateReactionResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createReaction(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteOwnReaction(final long guildId, final long channelId, final long messageId,
                                               final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteOwnReaction(getBotId(), guildId, channelId, messageId, emoji);
    }

    public GrpcRequest<Void> deleteOwnReaction(final long botId, final long guildId, final long channelId,
                                               final long messageId,
                                               final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteOwnReaction(botId, guildId, DeleteOwnReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    public GrpcRequest<Void> deleteOwnReaction(final long botId, final long guildId,
                                               final DeleteOwnReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteOwnReactionResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteOwnReaction(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteReaction(final long guildId, final long channelId, final long messageId,
                                            final long userId,
                                            final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteReaction(getBotId(), guildId, channelId, messageId, userId, emoji);
    }

    public GrpcRequest<Void> deleteReaction(final long botId, final long guildId, final long channelId,
                                            final long messageId,
                                            final long userId, final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteReaction(botId, guildId, DeleteUserReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setUserId(userId)
                .setEmoji(emoji)
                .build());
    }

    public GrpcRequest<Void> deleteReaction(final long botId, final long guildId,
                                            final DeleteUserReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteUserReactionResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteUserReaction(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteAllReactions(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteAllReactions(getBotId(), guildId, channelId, messageId);
    }

    public GrpcRequest<Void> deleteAllReactions(final long botId, final long guildId, final long channelId,
                                                final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteAllReactions(botId, guildId, DeleteAllReactionsRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .build());
    }

    public GrpcRequest<Void> deleteAllReactions(final long botId, final long guildId,
                                                final DeleteAllReactionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteAllReactionsResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteAllReactions(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteEmoteReactions(final long guildId, final long channelId, final long messageId,
                                                  final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteEmoteReactions(getBotId(), guildId, channelId, messageId, emoji);
    }

    public GrpcRequest<Void> deleteEmoteReactions(final long botId, final long guildId, final long channelId,
                                                  final long messageId,
                                                  final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteEmoteReactions(botId, guildId, DeleteAllReactionsForEmojiRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    public GrpcRequest<Void> deleteEmoteReactions(final long botId, final long guildId,
                                                  final DeleteAllReactionsForEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteAllReactionsForEmojiResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteAllReactionsForEmoji(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Message> editMessage(final long guildId, final EditMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return editMessage(getBotId(), guildId, request);
    }

    public GrpcRequest<Message> editMessage(final long botId, final long guildId, final EditMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<EditMessageResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.editMessage(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteMessage(guildId, channelId, messageId, null);
    }

    public GrpcRequest<Void> deleteMessage(final long guildId, final long channelId, final long messageId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteMessage(getBotId(), guildId, channelId, messageId, reason);
    }

    public GrpcRequest<Void> deleteMessage(final long botId, final long guildId, final long channelId,
                                           final long messageId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteMessage(botId, guildId, DeleteMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> deleteMessage(final long botId, final long guildId, final DeleteMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteMessageResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteMessage(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> bulkDeleteMessages(final long guildId, final long channelId, final List<Long> messageIds)
            throws GrpcRequestException, GrpcGatewayApiException {
        return bulkDeleteMessages(guildId, channelId, messageIds, null);
    }

    public GrpcRequest<Void> bulkDeleteMessages(final long guildId, final long channelId, final List<Long> messageIds,
                                                @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return bulkDeleteMessages(getBotId(), guildId, channelId, messageIds, reason);
    }

    public GrpcRequest<Void> bulkDeleteMessages(final long botId, final long guildId, final long channelId,
                                                final List<Long> messageIds, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return bulkDeleteMessages(botId, guildId, BulkDeleteMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .addAllMessageIds(messageIds)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> bulkDeleteMessages(final long botId, final long guildId,
                                                final BulkDeleteMessagesRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<BulkDeleteMessagesResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.bulkDeleteMessages(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> editChannelPermissions(final long guildId, final EditChannelPermissionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return editChannelPermissions(getBotId(), guildId, request);
    }

    public GrpcRequest<Void> editChannelPermissions(final long botId, final long guildId,
                                                    final EditChannelPermissionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<EditChannelPermissionsResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.editChannelPermissions(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<GuildInvite>> getChannelInvites(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getChannelInvites(getBotId(), guildId, channelId);
    }

    public GrpcRequest<List<GuildInvite>> getChannelInvites(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetChannelInvitesResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getChannelInvites(GetChannelInvitesRequest.newBuilder()
                                    .setChannelId(channelId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getInvitesList().stream()
                        .map(inviteData -> new GuildInvite(gatewayGrpcClient, botId, inviteData))
                        .collect(Collectors.toList());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<GuildInvite> createChannelInvite(final long guildId, final CreateChannelInviteRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createChannelInvite(getBotId(), guildId, request);
    }

    public GrpcRequest<GuildInvite> createChannelInvite(final long botId, final long guildId,
                                                        final CreateChannelInviteRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateChannelInviteResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createChannelInvite(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new GuildInvite(gatewayGrpcClient, botId, response.getData().getInvite());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteChannelPermission(final long guildId, final DeleteChannelPermissionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannelPermission(getBotId(), guildId, request);
    }

    public GrpcRequest<Void> deleteChannelPermission(final long botId, final long guildId,
                                                     final DeleteChannelPermissionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteChannelPermissionResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteChannelPermission(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Long> followNewsChannel(final long guildId, final long channelId, final long webhookId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return followNewsChannel(getBotId(), guildId, channelId, webhookId);
    }

    public GrpcRequest<Long> followNewsChannel(final long botId, final long guildId, final long channelId,
                                               final long webhookId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return followNewsChannel(botId, guildId, FollowNewsChannelRequest.newBuilder()
                .setChannelId(channelId)
                .setWebhookChannelId(webhookId)
                .build());
    }

    public GrpcRequest<Long> followNewsChannel(final long botId, final long guildId,
                                               final FollowNewsChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<FollowNewsChannelResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.followNewsChannel(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getChannelId();
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> startTyping(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return startTyping(getBotId(), guildId, channelId);
    }

    public GrpcRequest<Void> startTyping(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<TriggerTypingIndicatorResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.triggerTypingIndicator(TriggerTypingIndicatorRequest.newBuilder()
                                    .setChannelId(channelId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<Message>> getPinnedMessages(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getPinnedMessages(getBotId(), guildId, channelId);
    }

    public GrpcRequest<List<Message>> getPinnedMessages(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetPinnedMessagesResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getPinnedMessages(GetPinnedMessagesRequest.newBuilder()
                                    .setChannelId(channelId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getMessagesList()
                        .stream()
                        .map(messageData -> new Message(gatewayGrpcClient, botId, messageData))
                        .collect(Collectors.toList());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> pinMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return pinMessage(getBotId(), channelId, messageId, null);
    }

    public GrpcRequest<Void> pinMessage(final long guildId, final long channelId, final long messageId,
                                        @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return pinMessage(getBotId(), channelId, messageId, reason);
    }

    public GrpcRequest<Void> pinMessage(final long botId, final long guildId, final long channelId,
                                        final long messageId,
                                        @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return pinMessage(botId, guildId, AddPinnedChannelMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> pinMessage(final long botId, final long guildId,
                                        final AddPinnedChannelMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<AddPinnedChannelMessageResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.addPinnedChannelMessage(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> unpinMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return unpinMessage(guildId, channelId, messageId);
    }

    public GrpcRequest<Void> unpinMessage(final long guildId, final long channelId, final long messageId,
                                          @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return unpinMessage(getBotId(), guildId, channelId, messageId, reason);
    }

    public GrpcRequest<Void> unpinMessage(final long botId, final long guildId, final long channelId,
                                          final long messageId,
                                          @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return unpinMessage(botId, guildId, DeletePinnedChannelMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> unpinMessage(final long botId, final long guildId,
                                          final DeletePinnedChannelMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeletePinnedChannelMessageResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deletePinnedChannelMessage(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<List<Emoji>> listGuildEmojis(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return listGuildEmojis(getBotId(), guildId);
    }

    public GrpcRequest<List<Emoji>> listGuildEmojis(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ListGuildEmojisResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.listGuildEmojis(ListGuildEmojisRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return response.getData().getEmojisList().stream()
                        .map(emojiData -> new Emoji(gatewayGrpcClient, botId, emojiData))
                        .collect(Collectors.toList());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Emoji> getGuildEmoji(final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildEmoji(getBotId(), guildId, emoteId);
    }

    public GrpcRequest<Emoji> getGuildEmoji(final long botId, final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetGuildEmojiResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                                    .setEmojiId(emoteId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Emoji> createGuildEmoji(final long guildId, final CreateGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildEmoji(getBotId(), guildId, request);
    }

    public GrpcRequest<Emoji> createGuildEmoji(final long botId, final long guildId,
                                               final CreateGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateGuildEmojiResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createGuildEmoji(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Emoji> modifyGuildEmoji(final long guildId, final long emoteId, final String name)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(guildId, emoteId, name, null);
    }

    public GrpcRequest<Emoji> modifyGuildEmoji(final long guildId, final long emoteId, final String name,
                                               @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(getBotId(), guildId, emoteId, name, reason);
    }

    public GrpcRequest<Emoji> modifyGuildEmoji(final long botId, final long guildId, final long emoteId,
                                               final String name,
                                               @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(botId, guildId, ModifyGuildEmojiRequest.newBuilder()
                .setEmojiId(emoteId)
                .setName(name)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Emoji> modifyGuildEmoji(final long botId, final long guildId,
                                               final ModifyGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyGuildEmojiResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyGuildEmoji(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> deleteGuildEmoji(final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildEmoji(guildId, emoteId, (String) null);
    }

    public GrpcRequest<Void> deleteGuildEmoji(final long guildId, final long emoteId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildEmoji(getBotId(), guildId, emoteId, reason);
    }

    public GrpcRequest<Void> deleteGuildEmoji(final long botId, final long guildId, final long emoteId,
                                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildEmoji(botId, guildId, DeleteGuildEmojiRequest.newBuilder()
                .setEmojiId(emoteId)
                .setAuditLogReason(reason)
                .build());
    }

    public GrpcRequest<Void> deleteGuildEmoji(final long botId, final long guildId,
                                              final DeleteGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<DeleteGuildEmojiResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.deleteGuildEmoji(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<User> getSelfUser(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getSelfUser(getBotId(), guildId);
    }

    public GrpcRequest<User> getSelfUser(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetCurrentUserResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getCurrentUser(GetCurrentUserRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new User(gatewayGrpcClient, botId, response.getData().getUser());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<User> getUser(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getUser(getBotId(), guildId, userId);
    }

    public GrpcRequest<User> getUser(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<GetUserResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getUser(GetUserRequest.newBuilder()
                                    .setUserId(userId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new User(gatewayGrpcClient, botId, response.getData().getUser());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<User> modifySelfUser(final long guildId, final ModifyCurrentUserRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifySelfUser(getBotId(), guildId, request);
    }

    public GrpcRequest<User> modifySelfUser(final long botId, final long guildId,
                                            final ModifyCurrentUserRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<ModifyCurrentUserResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.modifyCurrentUser(request));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new User(gatewayGrpcClient, botId, response.getData().getUser());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Void> leaveGuild(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return leaveGuild(getBotId(), guildId);
    }

    public GrpcRequest<Void> leaveGuild(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<LeaveGuildResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.leaveGuild(LeaveGuildRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return null;
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    public GrpcRequest<Channel> createDmChannel(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createDmChannel(getBotId(), guildId, userId);
    }

    public GrpcRequest<Channel> createDmChannel(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ListenableFuture<CreateDmResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.createDm(CreateDmRequest.newBuilder()
                                    .setRecipientId(userId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (response.hasError()) {
                    throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
                }
                return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

}
