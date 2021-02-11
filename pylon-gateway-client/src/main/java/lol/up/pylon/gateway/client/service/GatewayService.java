package lol.up.pylon.gateway.client.service;

import bot.pylon.proto.discord.v1.gateway.*;
import bot.pylon.proto.gateway.v1.service.GatewayGrpc;
import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Emoji;
import lol.up.pylon.gateway.client.entity.Guild;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.event.EventExecutorService;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.service.request.GrpcRequestImpl;
import lol.up.pylon.gateway.client.util.CompletableFutureStreamObserver;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class GatewayService {

    private static final Logger log = LoggerFactory.getLogger(GatewayService.class);

    private final GatewayGrpc.GatewayStub client;
    private final GatewayGrpcClient gatewayGrpcClient;
    private final ExecutorService executorService;
    private final boolean warnWithoutContext;

    public GatewayService(final GatewayGrpcClient gatewayGrpcClient,
                          final GatewayGrpc.GatewayStub client,
                          final ExecutorService executorService, final boolean warnWithoutContext) {
        this.gatewayGrpcClient = gatewayGrpcClient;
        this.executorService = new EventExecutorService(executorService, EventContext.localContext());
        this.warnWithoutContext = warnWithoutContext;
        this.client = client.withCallCredentials(new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                final Metadata metadata = new Metadata();
                metadata.put(Constants.METADATA_BOT_ID, String.valueOf(Constants.CTX_BOT_ID.get()));
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
        if(warnWithoutContext)
        log.warn("Missing event context in current thread. Did you manually create threads? Consider using " +
                "AbstractEventReceiver#async instead!", new RuntimeException());
        return gatewayGrpcClient.getDefaultBotId();
    }

    @CheckReturnValue
    public GrpcRequest<Void> updateVoiceState(final long guildId, final long channelId) throws GrpcRequestException {
        return updateVoiceState(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcRequest<Void> updateVoiceState(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        return updateVoiceState(botId, guildId, channelId, false, false);
    }

    @CheckReturnValue
    public GrpcRequest<Void> updateVoiceState(final long botId, final long guildId, final long channelId,
                                              final boolean selfMute, final boolean selfDeaf) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<UpdateVoiceStateResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValue(Constants.CTX_BOT_ID, botId)
                    .run(() -> client.updateVoiceState(UpdateVoiceStateRequest.newBuilder()
                            .setGuildId(guildId)
                            .setChannelId(channelId)
                            .setSelfMute(selfMute)
                            .setSelfDeaf(selfDeaf)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> null);
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcRequest<Void> updateStatus(final UpdateStatusRequest request) throws GrpcRequestException {
        return updateStatus(getBotId(), request);
    }

    @CheckReturnValue
    public GrpcRequest<Void> updateStatus(final long botId, final UpdateStatusRequest request) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<UpdateStatusResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValue(Constants.CTX_BOT_ID, botId)
                    .run(() -> client.updateStatus(request, asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> null);
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcRequest<List<Guild>> getMutualGuilds(final long userId) throws GrpcRequestException {
        return getMutualGuilds(getBotId(), userId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Guild>> getMutualGuilds(final long botId, final long userId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetUserMutualGuildsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValue(Constants.CTX_BOT_ID, botId)
                    .run(() -> client.findUserMutualGuilds(GetUserMutualGuildsRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getGuildsList().stream()
                    .map(guildData -> new Guild(gatewayGrpcClient, botId, guildData))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    @CheckReturnValue
    public GrpcRequest<Emoji> findEmojiById(final long emojiId) throws GrpcRequestException {
        return findEmojiById(getBotId(), emojiId);
    }

    @CheckReturnValue
    public GrpcRequest<Emoji> findEmojiById(final long botId, final long emojiId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<FindEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValue(Constants.CTX_BOT_ID, botId)
                    .run(() -> client.findEmoji(FindEmojiRequest.newBuilder()
                            .setEmojiId(emojiId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if(!response.hasEmoji()) {
                    return null;
                }
                return new Emoji(gatewayGrpcClient, botId, response.getEmoji());
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }
}
