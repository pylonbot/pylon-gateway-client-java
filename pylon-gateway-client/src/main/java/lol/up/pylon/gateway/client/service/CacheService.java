package lol.up.pylon.gateway.client.service;

import bot.pylon.proto.discord.v1.cache.*;
import bot.pylon.proto.discord.v1.model.*;
import bot.pylon.proto.gateway.v1.service.GatewayCacheGrpc;
import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.*;
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

public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final GatewayCacheGrpc.GatewayCacheStub client;
    private final GatewayGrpcClient gatewayGrpcClient;
    private final ExecutorService executorService;

    public CacheService(final GatewayGrpcClient gatewayGrpcClient,
                        final GatewayCacheGrpc.GatewayCacheStub client,
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

    // Guilds (1x + Overload)
    @CheckReturnValue
    public GrpcRequest<Guild> getGuild(final long guildId) throws GrpcRequestException {
        return getGuild(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcRequest<Guild> getGuild(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetGuildResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuild(GetGuildRequest.newBuilder().build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasGuild()) {
                    return null;
                }
                final GuildData data = response.getGuild();
                if (data == null) {
                    return null;
                }
                return new Guild(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Channels (3x + Overloads)
    // - Get (1 Overload)
    @CheckReturnValue
    public GrpcRequest<Channel> getChannel(final long guildId, final long channelId) throws GrpcRequestException {
        return getChannel(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcRequest<Channel> getChannel(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetGuildChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasChannel()) {
                    return null;
                }
                final ChannelData data = response.getChannel();
                return new Channel(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - List (1 Overload)
    @CheckReturnValue
    public GrpcRequest<List<Channel>> listGuildChannels(final long guildId) throws GrpcRequestException {
        return listGuildChannels(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Channel>> listGuildChannels(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<ListGuildChannelsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildChannels(ListGuildChannelsRequest.newBuilder().build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getChannelsList().stream()
                    .map(channel -> new Channel(gatewayGrpcClient, botId, channel))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - DM (1 Overload)
    @CheckReturnValue
    public GrpcRequest<Channel> getDmChannel(final long channelId, final long userId) throws GrpcRequestException {
        return getDmChannel(getBotId(), channelId, userId);
    }

    @CheckReturnValue
    public GrpcRequest<Channel> getDmChannel(final long botId, final long channelId, final long userId) throws GrpcRequestException {
        // TODO: fix dm channel impl
        try {
            final CompletableFutureStreamObserver<GetGuildChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                    .run(() -> client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasChannel()) {
                    return null;
                }
                final ChannelData data = response.getChannel();
                return new Channel(gatewayGrpcClient, botId, data, userId);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }


    // Guild Members (2x + Overloads)
    // - Get (1 Overload)
    @CheckReturnValue
    public GrpcRequest<Member> getMember(final long guildId, final long userId) throws GrpcRequestException {
        return getMember(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcRequest<Member> getMember(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetGuildMemberResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildMember(GetGuildMemberRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasMember()) {
                    return null;
                }
                final MemberData data = response.getMember();
                return new Member(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - List (5 Overloads)
    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembers(final long guildId) throws GrpcRequestException {
        return listGuildMembers(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembersAfter(final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(getBotId(), guildId, after);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembersAfter(final long guildId, long after, int limit) throws GrpcRequestException {
        return listGuildMembersAfter(getBotId(), guildId, after, limit);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembers(final long botId, final long guildId) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, 0);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembersAfter(final long botId, final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, after, 0);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembersAfter(final long botId, final long guildId, final long after,
                                                           final int limit) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<ListGuildMembersResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildMembers(ListGuildMembersRequest.newBuilder()
                            .setAfter(after)
                            .setLimit(limit)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getMembersList().stream()
                    .map(member -> new Member(gatewayGrpcClient, botId, member))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Guild Member Properties (2x + Overloads)
    // - Get ( 1 Overload)
    @CheckReturnValue
    public GrpcRequest<Role> getRole(final long guildId, final long roleId) throws GrpcRequestException {
        return getRole(getBotId(), guildId, roleId);
    }

    @CheckReturnValue
    public GrpcRequest<Role> getRole(final long botId, final long guildId, final long roleId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetGuildRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildRole(GetGuildRoleRequest.newBuilder()
                            .setRoleId(roleId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasRole()) {
                    return null;
                }
                final RoleData data = response.getRole();
                return new Role(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - List (1 Overload)
    @CheckReturnValue
    public GrpcRequest<List<Role>> listGuildRoles(final long guildId) throws GrpcRequestException {
        return listGuildRoles(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Role>> listGuildRoles(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<ListGuildRolesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildRoles(ListGuildRolesRequest.newBuilder().build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getRolesList().stream()
                    .map(role -> new Role(gatewayGrpcClient, botId, role))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Emojis (2x + Overloads)
    // - Get (1 Overload)
    @CheckReturnValue
    public GrpcRequest<Emoji> getEmoji(final long guildId, final long emojiId) throws GrpcRequestException {
        return getEmoji(getBotId(), guildId, emojiId);
    }

    @CheckReturnValue
    public GrpcRequest<Emoji> getEmoji(final long botId, final long guildId, final long emojiId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetGuildEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                            .setEmojiId(emojiId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasEmoji()) {
                    return null;
                }
                final EmojiData data = response.getEmoji();
                return new Emoji(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - List (1 Overload)
    @CheckReturnValue
    public GrpcRequest<List<Emoji>> listGuildEmojis(final long guildId) throws GrpcRequestException {
        return listGuildEmojis(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcRequest<List<Emoji>> listGuildEmojis(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<ListGuildEmojisResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildEmojis(ListGuildEmojisRequest.newBuilder().build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getEmojisList().stream()
                    .map(emoji -> new Emoji(gatewayGrpcClient, botId, emoji))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Users (1x + 1 Overload)
    @CheckReturnValue
    public GrpcRequest<User> getUser(final long userId) throws GrpcRequestException {
        return getUser(getBotId(), userId);
    }

    @CheckReturnValue
    public GrpcRequest<User> getUser(final long botId, final long userId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetUserResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                    .run(() -> client.getUser(GetUserRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasUser()) {
                    return null;
                }
                final UserData data = response.getUser();
                return new User(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // VoiceStates (2x + Overloads)
    // - Get (Overload)
    @CheckReturnValue
    public GrpcRequest<MemberVoiceState> getVoiceState(final long guildId, final long userId) throws GrpcRequestException {
        return getVoiceState(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcRequest<MemberVoiceState> getVoiceState(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<GetGuildMemberVoiceStateResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildMemberVoiceState(GetGuildMemberVoiceStateRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasVoiceStateData()) {
                    return null;
                }
                final VoiceStateData data = response.getVoiceStateData();
                return new MemberVoiceState(gatewayGrpcClient, botId, data);
            });
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - List (1 Overload)
    @CheckReturnValue
    public GrpcRequest<List<MemberVoiceState>> listChannelVoiceStates(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelVoiceStates(getBotId(), guildId, channelId);
    }

    @CheckReturnValue
    public GrpcRequest<List<MemberVoiceState>> listChannelVoiceStates(final long botId, final long guildId,
                                                                      final long channelId) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<ListGuildChannelVoiceStatesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();
            Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildChannelVoiceStates(ListGuildChannelVoiceStatesRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse));
            return new GrpcRequestImpl<>(executorService, asyncResponse,
                    response -> response.getVoiceStatesDataList().stream()
                            .map(voiceStateData -> new MemberVoiceState(gatewayGrpcClient, botId, voiceStateData))
                            .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }
}
