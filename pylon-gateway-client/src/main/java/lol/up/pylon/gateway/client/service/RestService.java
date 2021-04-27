package lol.up.pylon.gateway.client.service;

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
import lol.up.pylon.gateway.client.event.ScheduledEventExecutorService;
import lol.up.pylon.gateway.client.exception.*;
import lol.up.pylon.gateway.client.service.request.GrpcApiRequest;
import lol.up.pylon.gateway.client.service.request.GrpcApiRequestImpl;
import lol.up.pylon.gateway.client.util.CompletableFutureStreamObserver;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class RestService {

    private static final Logger log = LoggerFactory.getLogger(RestService.class);

    private final GatewayRestGrpc.GatewayRestStub client;
    private final GatewayGrpcClient gatewayGrpcClient;
    private final ScheduledExecutorService executorService;
    private final boolean warnWithoutContext;

    public RestService(final GatewayGrpcClient gatewayGrpcClient,
                       final GatewayRestGrpc.GatewayRestStub client,
                       final ScheduledExecutorService executorService, final boolean warnWithoutContext,
                       final Duration maxRatelimitWaitDuration) {
        this.gatewayGrpcClient = gatewayGrpcClient;
        this.executorService = new ScheduledEventExecutorService(executorService, EventContext.localContext());
        this.warnWithoutContext = warnWithoutContext;
        this.client = client.withCallCredentials(new CallCredentials() {
            private final String timeout = String.valueOf(maxRatelimitWaitDuration.toMillis());

            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                final Metadata metadata = new Metadata();
                metadata.put(Constants.METADATA_BOT_ID, String.valueOf(Constants.CTX_BOT_ID.get()));
                metadata.put(Constants.METADATA_GUILD_ID, String.valueOf(Constants.CTX_GUILD_ID.get()));
                metadata.put(Metadata.Key.of("x-pylon-max-ratelimit-ms", Metadata.ASCII_STRING_MARSHALLER), timeout);
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
        if (warnWithoutContext) {
            log.warn("Missing event context in current thread. Did you manually create threads? Consider using " +
                    "AbstractEventReceiver#async instead!", new RuntimeException());
        }
        return gatewayGrpcClient.getDefaultBotId();
    }

    private GrpcGatewayApiException createApiException(final RestError apiError, final GrpcException source) {
        switch (apiError.getErrorTypeCase()) {
            case UNKNOWN_ERROR:
                return new GrpcGatewayApiUnknownErrorException(apiError,
                        "An unknown error occurred during REST request: " +
                                "HTTPStatus: " + apiError.getUnknownError().getHttpStatus() + " | " +
                                "ErrorCode: " + apiError.getUnknownError().getCode() + " | " +
                                "Message: " + apiError.getUnknownError().getMessage());
            case VALIDATION_ERROR:
                final StringBuilder fieldsError = new StringBuilder("Fields:");
                apiError.getValidationError().getFieldsList().forEach(field -> fieldsError.append(" Field(")
                        .append("path=").append(field.getPath()).append(",")
                        .append("code=").append(field.getCode()).append(",")
                        .append("message=").append(field.getMessage())
                        .append(")"));
                return new GrpcGatewayApiValidationErrorException(apiError,
                        "A validation error occurred during REST request: " +
                                "Message: " + apiError.getValidationError().getMessage() + " | " +
                                "Fields: " + fieldsError.toString());
            case RESOURCE_NOT_FOUND:
                return new GrpcGatewayApiResourceNotFoundException(apiError,
                        "A resource was not found during REST request: " +
                                "ErrorCode: " + apiError.getResourceNotFound().getCode() + " | " +
                                "Message: " + apiError.getResourceNotFound().getMessage());
            case ACCESS_DENIED:
                return new GrpcGatewayApiAccessDeniedException(apiError,
                        "Access to a resource was denied during REST request: " +
                                "ErrorCode: " + apiError.getAccessDenied().getCode() + " | " +
                                "Message: " + apiError.getAccessDenied().getMessage());
            case RATE_LIMITED:
                return new GrpcGatewayApiRateLimitedException(apiError,
                        "A request was rate-limited during REST request: " +
                                "Global: " + apiError.getRateLimited().getGlobal() + " | " +
                                "Retry-At: " + apiError.getRateLimited().getRetryAt());
            case ERRORTYPE_NOT_SET:
            default:
                return new GrpcGatewayApiException(apiError, "An unknown error type occurred!", source);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Guild> modifyGuild(final long guildId, final ModifyGuildRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuild(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Guild> modifyGuild(final long botId, final long guildId,
                                          final ModifyGuildRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyGuildResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Guild(gatewayGrpcClient, botId, response.getData().getGuild());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyGuild(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Channel> createChannel(final long guildId, final CreateGuildChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createChannel(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Channel> createChannel(final long botId, final long guildId,
                                              final CreateGuildChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateGuildChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createGuildChannel(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> modifyChannelPositions(final long guildId,
                                                    final ModifyGuildChannelPositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyChannelPositions(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> modifyChannelPositions(final long botId, final long guildId,
                                                    final ModifyGuildChannelPositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyGuildChannelPositionsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyGuildChannelPositions(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Boolean> addGuildMember(final long guildId, final AddGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addGuildMember(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Boolean> addGuildMember(final long botId, final long guildId,
                                               final AddGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<AddGuildMemberResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getAdded();
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.addGuildMember(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> modifyGuildMember(final long guildId, final ModifyGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildMember(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> modifyGuildMember(final long botId, final long guildId,
                                               final ModifyGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyGuildMemberResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyGuildMember(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> changeSelfNickname(final long guildId, final String nickName)
            throws GrpcRequestException, GrpcGatewayApiException {
        return changeSelfNickname(getBotId(), guildId, nickName);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> changeSelfNickname(final long botId, final long guildId, final String nickName)
            throws GrpcRequestException, GrpcGatewayApiException {
        return changeSelfNickname(botId, guildId, ModifyCurrentUserNickRequest.newBuilder()
                .setNick(nickName)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> changeSelfNickname(final long botId, final long guildId,
                                                final ModifyCurrentUserNickRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyCurrentUserNickResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyCurrentUserNick(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addMemberRole(final long guildId, final long memberId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addMemberRole(guildId, memberId, roleId, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addMemberRole(final long guildId, final long memberId, final long roleId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addMemberRole(getBotId(), guildId, memberId, roleId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addMemberRole(final long botId, final long guildId, final long memberId, final long roleId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return addMemberRole(botId, guildId, AddGuildMemberRoleRequest.newBuilder()
                .setUserId(memberId)
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addMemberRole(final long botId, final long guildId,
                                           final AddGuildMemberRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<AddGuildMemberRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.addGuildMemberRole(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeMemberRole(final long guildId, final long memberId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return removeMemberRole(guildId, memberId, roleId, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeMemberRole(final long guildId, final long memberId, final long roleId,
                                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return removeMemberRole(getBotId(), guildId, memberId, roleId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeMemberRole(final long botId, final long guildId, final long memberId,
                                              final long roleId,
                                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return removeMemberRole(botId, guildId, RemoveGuildMemberRoleRequest.newBuilder()
                .setUserId(memberId)
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeMemberRole(final long botId, final long guildId,
                                              final RemoveGuildMemberRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<RemoveGuildMemberRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.removeGuildMemberRole(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeGuildMember(final long botId, final long guildId,
                                               final RemoveGuildMemberRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<RemoveGuildMemberResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.removeGuildMember(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Ban>> getGuildBans(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBans(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Ban>> getGuildBans(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetGuildBansResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getBansList()
                        .stream()
                        .map(guildBanData -> new Ban(gatewayGrpcClient, botId, guildId, guildBanData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildBans(GetGuildBansRequest.newBuilder().build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Ban> getGuildBan(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBan(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Ban> getGuildBan(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildBan(botId, guildId, GetGuildBanRequest.newBuilder()
                .setUserId(userId)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Ban> getGuildBan(final long botId, final long guildId, final GetGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetGuildBanResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                if (!response.hasData() || !response.getData().hasBan()) {
                    return null;
                }
                return new Ban(gatewayGrpcClient, botId, guildId, response.getData().getBan());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildBan(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createGuildBan(final long guildId, final long userId, final int deleteDays)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildBan(guildId, userId, deleteDays, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createGuildBan(final long guildId, final long userId, final int deleteDays,
                                            @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildBan(getBotId(), guildId, userId, deleteDays, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createGuildBan(final long botId, final long guildId, final long userId,
                                            final int deleteDays,
                                            @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildBan(botId, guildId, CreateGuildBanRequest.newBuilder()
                .setUserId(userId)
                .setDeleteMessageDays(deleteDays)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createGuildBan(final long botId, final long guildId, final CreateGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateGuildBanResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createGuildBan(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeGuildBan(final long botId, final long guildId, final RemoveGuildBanRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<RemoveGuildBanResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.removeGuildBan(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Role> createGuildRole(final long guildId, final CreateGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildRole(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Role> createGuildRole(final long botId, final long guildId, final CreateGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateGuildRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Role(gatewayGrpcClient, botId, response.getData().getRole());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createGuildRole(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Role>> modifyGuildRolePositions(final long guildId,
                                                            final ModifyGuildRolePositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildRolePositions(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Role>> modifyGuildRolePositions(final long botId, final long guildId,
                                                            final ModifyGuildRolePositionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyGuildRolePositionsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getRolesList().stream()
                        .map(roleData -> new Role(gatewayGrpcClient, botId, roleData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyGuildRolePositions(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Role> modifyGuildRole(final long guildId, final ModifyGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildRole(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Role> modifyGuildRole(final long botId, final long guildId, final ModifyGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyGuildRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Role(gatewayGrpcClient, botId, response.getData().getRole());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyGuildRole(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildRole(final long guildId, final long roleId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildRole(guildId, roleId, (String) null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildRole(final long guildId, final long roleId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildRole(getBotId(), guildId, roleId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildRole(final long botId, final long guildId, final long roleId,
                                             @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildRole(botId, guildId, DeleteGuildRoleRequest.newBuilder()
                .setRoleId(roleId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildRole(final long botId, final long guildId, final DeleteGuildRoleRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteGuildRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteGuildRole(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Integer> getGuildPruneCount(final long guildId, final int days, final List<Long> roles)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildPruneCount(getBotId(), guildId, days, roles);
    }

    @CheckReturnValue
    public GrpcApiRequest<Integer> getGuildPruneCount(final long botId, final long guildId, final int days,
                                                   final List<Long> roles)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildPruneCount(botId, guildId, GetGuildPruneCountRequest.newBuilder()
                .setDays(days)
                .setIncludeRoles(SnowflakeListValue.newBuilder()
                        .addAllValues(roles)
                        .build())
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Integer> getGuildPruneCount(final long botId, final long guildId,
                                                   final GetGuildPruneCountRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetGuildPruneCountResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getSerializedSize(); // todo ehhhhh?
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildPruneCount(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> beginGuildPrune(final long guildId, final BeginGuildPruneRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return beginGuildPrune(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> beginGuildPrune(final long botId, final long guildId, final BeginGuildPruneRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<BeginGuildPruneResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.beginGuildPrune(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<String>> getGuildVoiceRegions(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildVoiceRegions(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<String>> getGuildVoiceRegions(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetGuildVoiceRegionsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getRegionsList().asByteStringList().stream()
                        .map(ByteString::toStringUtf8)
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildVoiceRegions(GetGuildVoiceRegionsRequest.newBuilder().build(),
                            asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<GuildInvite>> getGuildInvites(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildInvites(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<GuildInvite>> getGuildInvites(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetGuildInvitesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getInvitesList().stream()
                        .map(inviteData -> new GuildInvite(gatewayGrpcClient, botId, inviteData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildInvites(GetGuildInvitesRequest.newBuilder().build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Channel> modifyChannel(final long guildId, final ModifyChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyChannel(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Channel> modifyChannel(final long botId, final long guildId, final ModifyChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Channel(gatewayGrpcClient, botId, response.getData().getChannel());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyChannel(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteChannel(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannel(guildId, channelId, (String) null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteChannel(final long guildId, final long channelId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannel(getBotId(), guildId, channelId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteChannel(final long botId, final long guildId, final long channelId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannel(botId, guildId, DeleteChannelRequest.newBuilder()
                .setChannelId(channelId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteChannel(final long botId, final long guildId, final DeleteChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteChannel(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesAround(final long guildId, final long channelId,
                                                        final long snowflake) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessagesAround(getBotId(), guildId, channelId, snowflake);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesAround(final long botId, final long guildId, final long channelId,
                                                        final long snowflake) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessagesAround(botId, guildId, channelId, snowflake, 100);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesAround(final long botId, final long guildId, final long channelId,
                                                        final long snowflake, final int limit) throws GrpcRequestException, GrpcGatewayApiException {
        return getMessages(botId, guildId, GetChannelMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .setAround(snowflake)
                .setLimit(limit)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesAfter(final long guildId, final long channelId,
                                                       final long snowflake) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessagesAfter(getBotId(), guildId, channelId, snowflake);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesAfter(final long botId, final long guildId, final long channelId,
                                                       final long snowflake) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessagesAfter(botId, guildId, channelId, snowflake, 100);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesAfter(final long botId, final long guildId, final long channelId,
                                                       final long snowflake, final int limit) throws GrpcRequestException, GrpcGatewayApiException {
        return getMessages(botId, guildId, GetChannelMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .setAfter(snowflake)
                .setLimit(limit)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesBefore(final long guildId, final long channelId,
                                                        final long snowflake) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessagesBefore(getBotId(), guildId, channelId, snowflake);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesBefore(final long botId, final long guildId, final long channelId,
                                                        final long snowflake) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessagesBefore(botId, guildId, channelId, snowflake, 100);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessagesBefore(final long botId, final long guildId, final long channelId,
                                                        final long snowflake, final int limit) throws GrpcRequestException, GrpcGatewayApiException {
        return getMessages(botId, guildId, GetChannelMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .setBefore(snowflake)
                .setLimit(limit)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessages(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getMessages(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessages(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getMessages(botId, guildId, channelId, 100);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessages(final long botId, final long guildId, final long channelId,
                                                  final int limit) throws GrpcRequestException,
            GrpcGatewayApiException {
        return getMessages(botId, guildId, GetChannelMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .setLimit(limit)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getMessages(final long botId, final long guildId,
                                                  final GetChannelMessagesRequest request) throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetChannelMessagesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getMessagesList().stream()
                        .map(messageData -> new Message(gatewayGrpcClient, botId, messageData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getChannelMessages(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> getMessage(final long guildId, final long channelId, final long messageId) {
        return getMessage(getBotId(), guildId, channelId, messageId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> getMessage(final long botId, final long guildId, final long channelId,
                                           final long messageId) throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetChannelMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                if (!response.getData().hasMessage()) {
                    return null;
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getChannelMessage(GetChannelMessageRequest.newBuilder()
                            .setChannelId(channelId)
                            .setMessageId(messageId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> createMessage(final long guildId, final CreateMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createMessage(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> createMessage(final long botId, final long guildId, final CreateMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createMessage(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> crosspostMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return crosspostMessage(getBotId(), guildId, channelId, messageId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> crosspostMessage(final long botId, final long guildId, final long channelId,
                                                 final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return crosspostMessage(botId, guildId, CrosspostMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> crosspostMessage(final long botId, final long guildId,
                                                 final CrosspostMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CrosspostMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.crosspostMessage(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createReaction(final long guildId, final long channelId, final long messageId,
                                            final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createReaction(getBotId(), guildId, channelId, messageId, emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createReaction(final long botId, final long guildId, final long channelId,
                                            final long messageId,
                                            final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createReaction(botId, guildId, CreateReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> createReaction(final long botId, final long guildId, final CreateReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateReactionResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createReaction(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteOwnReaction(final long guildId, final long channelId, final long messageId,
                                               final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteOwnReaction(getBotId(), guildId, channelId, messageId, emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteOwnReaction(final long botId, final long guildId, final long channelId,
                                               final long messageId,
                                               final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteOwnReaction(botId, guildId, DeleteOwnReactionRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteOwnReaction(final long botId, final long guildId,
                                               final DeleteOwnReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteOwnReactionResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteOwnReaction(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteReaction(final long guildId, final long channelId, final long messageId,
                                            final long userId,
                                            final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteReaction(getBotId(), guildId, channelId, messageId, userId, emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteReaction(final long botId, final long guildId, final long channelId,
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

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteReaction(final long botId, final long guildId,
                                            final DeleteUserReactionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteUserReactionResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteUserReaction(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteAllReactions(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteAllReactions(getBotId(), guildId, channelId, messageId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteAllReactions(final long botId, final long guildId, final long channelId,
                                                final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteAllReactions(botId, guildId, DeleteAllReactionsRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteAllReactions(final long botId, final long guildId,
                                                final DeleteAllReactionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteAllReactionsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteAllReactions(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteEmoteReactions(final long guildId, final long channelId, final long messageId,
                                                  final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteEmoteReactions(getBotId(), guildId, channelId, messageId, emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteEmoteReactions(final long botId, final long guildId, final long channelId,
                                                  final long messageId,
                                                  final String emoji)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteEmoteReactions(botId, guildId, DeleteAllReactionsForEmojiRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setEmoji(emoji)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteEmoteReactions(final long botId, final long guildId,
                                                  final DeleteAllReactionsForEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteAllReactionsForEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteAllReactionsForEmoji(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> editMessage(final long guildId, final EditMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return editMessage(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Message> editMessage(final long botId, final long guildId, final EditMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<EditMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Message(gatewayGrpcClient, botId, response.getData().getMessage());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.editMessage(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteMessage(guildId, channelId, messageId, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteMessage(final long guildId, final long channelId, final long messageId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteMessage(getBotId(), guildId, channelId, messageId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteMessage(final long botId, final long guildId, final long channelId,
                                           final long messageId,
                                           @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteMessage(botId, guildId, DeleteMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteMessage(final long botId, final long guildId, final DeleteMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteMessage(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> bulkDeleteMessages(final long guildId, final long channelId, final List<Long> messageIds)
            throws GrpcRequestException, GrpcGatewayApiException {
        return bulkDeleteMessages(guildId, channelId, messageIds, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> bulkDeleteMessages(final long guildId, final long channelId, final List<Long> messageIds,
                                                @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return bulkDeleteMessages(getBotId(), guildId, channelId, messageIds, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> bulkDeleteMessages(final long botId, final long guildId, final long channelId,
                                                final List<Long> messageIds, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return bulkDeleteMessages(botId, guildId, BulkDeleteMessagesRequest.newBuilder()
                .setChannelId(channelId)
                .addAllMessageIds(messageIds)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> bulkDeleteMessages(final long botId, final long guildId,
                                                final BulkDeleteMessagesRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<BulkDeleteMessagesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.bulkDeleteMessages(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> editChannelPermissions(final long guildId, final EditChannelPermissionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return editChannelPermissions(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> editChannelPermissions(final long botId, final long guildId,
                                                    final EditChannelPermissionsRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<EditChannelPermissionsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.editChannelPermissions(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<GuildInvite>> getChannelInvites(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getChannelInvites(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<GuildInvite>> getChannelInvites(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetChannelInvitesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getInvitesList().stream()
                        .map(inviteData -> new GuildInvite(gatewayGrpcClient, botId, inviteData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getChannelInvites(GetChannelInvitesRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<GuildInvite> createChannelInvite(final long guildId, final CreateChannelInviteRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createChannelInvite(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<GuildInvite> createChannelInvite(final long botId, final long guildId,
                                                        final CreateChannelInviteRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateChannelInviteResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new GuildInvite(gatewayGrpcClient, botId, response.getData().getInvite());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createChannelInvite(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteChannelPermission(final long guildId, final DeleteChannelPermissionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteChannelPermission(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteChannelPermission(final long botId, final long guildId,
                                                     final DeleteChannelPermissionRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteChannelPermissionResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteChannelPermission(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Long> followNewsChannel(final long guildId, final long channelId, final long webhookId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return followNewsChannel(getBotId(), guildId, channelId, webhookId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Long> followNewsChannel(final long botId, final long guildId, final long channelId,
                                               final long webhookId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return followNewsChannel(botId, guildId, FollowNewsChannelRequest.newBuilder()
                .setChannelId(channelId)
                .setWebhookChannelId(webhookId)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Long> followNewsChannel(final long botId, final long guildId,
                                               final FollowNewsChannelRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<FollowNewsChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getChannelId();
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.followNewsChannel(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> startTyping(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return startTyping(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> startTyping(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<TriggerTypingIndicatorResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.triggerTypingIndicator(TriggerTypingIndicatorRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getPinnedMessages(final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getPinnedMessages(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Message>> getPinnedMessages(final long botId, final long guildId, final long channelId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetPinnedMessagesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getMessagesList()
                        .stream()
                        .map(messageData -> new Message(gatewayGrpcClient, botId, messageData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getPinnedMessages(GetPinnedMessagesRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> pinMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return pinMessage(getBotId(), channelId, messageId, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> pinMessage(final long guildId, final long channelId, final long messageId,
                                        @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return pinMessage(getBotId(), channelId, messageId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> pinMessage(final long botId, final long guildId, final long channelId,
                                        final long messageId,
                                        @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return pinMessage(botId, guildId, AddPinnedChannelMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> pinMessage(final long botId, final long guildId,
                                        final AddPinnedChannelMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<AddPinnedChannelMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.addPinnedChannelMessage(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> unpinMessage(final long guildId, final long channelId, final long messageId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return unpinMessage(guildId, channelId, messageId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> unpinMessage(final long guildId, final long channelId, final long messageId,
                                          @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return unpinMessage(getBotId(), guildId, channelId, messageId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> unpinMessage(final long botId, final long guildId, final long channelId,
                                          final long messageId,
                                          @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return unpinMessage(botId, guildId, DeletePinnedChannelMessageRequest.newBuilder()
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> unpinMessage(final long botId, final long guildId,
                                          final DeletePinnedChannelMessageRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeletePinnedChannelMessageResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deletePinnedChannelMessage(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Emoji>> listGuildEmojis(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return listGuildEmojis(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcApiRequest<List<Emoji>> listGuildEmojis(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ListGuildEmojisResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return response.getData().getEmojisList().stream()
                        .map(emojiData -> new Emoji(gatewayGrpcClient, botId, emojiData))
                        .collect(Collectors.toList());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildEmojis(ListGuildEmojisRequest.newBuilder().build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> getGuildEmoji(final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getGuildEmoji(getBotId(), guildId, emoteId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> getGuildEmoji(final long botId, final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetGuildEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                            .setEmojiId(emoteId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> createGuildEmoji(final long guildId, final CreateGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createGuildEmoji(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> createGuildEmoji(final long botId, final long guildId,
                                               final CreateGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateGuildEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.createGuildEmoji(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> modifyGuildEmoji(final long guildId, final long emoteId, final String name)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(guildId, emoteId, name, null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> modifyGuildEmoji(final long guildId, final long emoteId, final String name,
                                               @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(getBotId(), guildId, emoteId, name, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> modifyGuildEmoji(final long botId, final long guildId, final long emoteId,
                                               final String name,
                                               @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifyGuildEmoji(botId, guildId, ModifyGuildEmojiRequest.newBuilder()
                .setEmojiId(emoteId)
                .setName(name)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Emoji> modifyGuildEmoji(final long botId, final long guildId,
                                               final ModifyGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyGuildEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Emoji(gatewayGrpcClient, botId, response.getData().getEmoji());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyGuildEmoji(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildEmoji(final long guildId, final long emoteId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildEmoji(guildId, emoteId, (String) null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildEmoji(final long guildId, final long emoteId, @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildEmoji(getBotId(), guildId, emoteId, reason);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildEmoji(final long botId, final long guildId, final long emoteId,
                                              @Nullable final String reason)
            throws GrpcRequestException, GrpcGatewayApiException {
        return deleteGuildEmoji(botId, guildId, DeleteGuildEmojiRequest.newBuilder()
                .setEmojiId(emoteId)
                .setAuditLogReason(reason)
                .build());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> deleteGuildEmoji(final long botId, final long guildId,
                                              final DeleteGuildEmojiRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<DeleteGuildEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, request, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.deleteGuildEmoji(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<User> getSelfUser(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getSelfUser(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcApiRequest<User> getSelfUser(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetCurrentUserResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            ;
            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new User(gatewayGrpcClient, botId, response.getData().getUser());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getCurrentUser(GetCurrentUserRequest.newBuilder().build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<User> getUser(final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return getUser(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcApiRequest<User> getUser(final long botId, final long guildId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<GetUserResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new User(gatewayGrpcClient, botId, response.getData().getUser());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getUser(GetUserRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<User> modifySelfUser(final long guildId, final ModifyCurrentUserRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        return modifySelfUser(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcApiRequest<User> modifySelfUser(final long botId, final long guildId,
                                            final ModifyCurrentUserRequest request)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<ModifyCurrentUserResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new User(gatewayGrpcClient, botId, response.getData().getUser());
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.modifyCurrentUser(request, asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> leaveGuild(final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return leaveGuild(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> leaveGuild(final long botId, final long guildId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<LeaveGuildResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return null;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.leaveGuild(LeaveGuildRequest.newBuilder().build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcApiRequest<Channel> createDmChannel(final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        return createDmChannel(getBotId(), userId);
    }

    @CheckReturnValue
    public GrpcApiRequest<Channel> createDmChannel(final long botId, final long userId)
            throws GrpcRequestException, GrpcGatewayApiException {
        final GrpcException source = new GrpcException("Call trace");
        try {
            final CompletableFutureStreamObserver<CreateDmResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcApiRequestImpl<>(executorService, null, asyncResponse, response -> {
                if (response.hasError()) {
                    throw createApiException(response.getError(), source);
                }
                return new Channel(gatewayGrpcClient, botId, response.getData().getChannel(), userId);
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                    .run(() -> client.createDm(CreateDmRequest.newBuilder()
                            .setRecipientId(userId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

}
