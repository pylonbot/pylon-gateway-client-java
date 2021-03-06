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
import lol.up.pylon.gateway.client.service.request.ContinuationGrpcRequestImpl;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.service.request.GrpcRequestImpl;
import lol.up.pylon.gateway.client.util.CompletableFutureStreamObserver;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final GatewayCacheGrpc.GatewayCacheStub client;
    private final GatewayGrpcClient gatewayGrpcClient;
    private final ExecutorService executorService;
    private final boolean warnWithoutContext;

    public CacheService(final GatewayGrpcClient gatewayGrpcClient,
                        final GatewayCacheGrpc.GatewayCacheStub client,
                        final ExecutorService executorService, final boolean warnWithoutContext) {
        this.gatewayGrpcClient = gatewayGrpcClient;
        this.executorService = new EventExecutorService(executorService, EventContext.localContext());
        this.warnWithoutContext = warnWithoutContext;
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
        if (warnWithoutContext) {
            log.warn("Missing event context in current thread. Did you manually create threads? Consider using " +
                    "AbstractEventReceiver#async instead!", new RuntimeException());
        }
        return gatewayGrpcClient.getDefaultBotId();
    }

    // Guilds (1x + Overload)
    @CheckReturnValue
    public GrpcRequest<Guild> getGuild(final long guildId) throws GrpcRequestException {
        return getGuild(getBotId(), guildId);
    }

    @CheckReturnValue
    public GrpcRequest<Guild> getGuild(final long botId, final long guildId) throws GrpcRequestException {
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("guild", botId, guildId);
            final Guild guild = context.getContextObject(ctxKey);
            if (guild != null) {
                return new FinishedRequestImpl<>(guild);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasGuild()) {
                    return null;
                }
                final GuildData data = response.getGuild();
                if (data == null) {
                    return null;
                }
                final Guild guild = new Guild(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, guild);
                }
                return guild;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuild(GetGuildRequest.newBuilder().build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("channel", botId, guildId, channelId);
            final Channel channel = context.getContextObject(ctxKey);
            if (channel != null) {
                return new FinishedRequestImpl<>(channel);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasChannel()) {
                    return null;
                }
                final ChannelData data = response.getChannel();
                final Channel channel = new Channel(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, channel);
                }
                return channel;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("list_channels", botId, guildId);
            final List<Channel> channelList = context.getContextObject(ctxKey);
            if (channelList != null) {
                return new FinishedRequestImpl<>(channelList);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<ListGuildChannelsResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                final List<Channel> channelList = response.getChannelsList().stream()
                        .map(channel -> new Channel(gatewayGrpcClient, botId, channel))
                        .collect(Collectors.toList());
                if (context != null) {
                    context.populateContext(ctxKey, channelList);
                }
                return channelList;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildChannels(ListGuildChannelsRequest.newBuilder().build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("dm_channel", botId, channelId, userId);
            final Channel channel = context.getContextObject(ctxKey);
            if (channel != null) {
                return new FinishedRequestImpl<>(channel);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildChannelResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasChannel()) {
                    return null;
                }
                final ChannelData data = response.getChannel();
                final Channel channel = new Channel(gatewayGrpcClient, botId, data, userId);
                if (context != null) {
                    context.populateContext(ctxKey, channel);
                }
                return channel;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                    .run(() -> client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Guild Member Presences (1x + Overloads)
    @CheckReturnValue
    public GrpcRequest<Presence> getPresence(final long guildId, final long userId) throws GrpcRequestException {
        return getPresence(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcRequest<Presence> getPresence(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("presence", botId, guildId, userId);
            final Presence presence = context.getContextObject(ctxKey);
            if (presence != null) {
                return new FinishedRequestImpl<>(presence);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildMemberPresenceResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                final PresenceData data;
                if (response.hasPresence()) {
                    data = response.getPresence();
                } else {
                    data = PresenceData.newBuilder()
                            .setGuildId(guildId)
                            .setUserId(userId)
                            .setStatus(PresenceData.OnlineStatus.OFFLINE)
                            .build();
                }
                final Presence presence = new Presence(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, presence);
                }
                return presence;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildMemberPresence(GetGuildMemberPresenceRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Guild Members (3x + Overloads)
    // - Get (1 Overload)
    @CheckReturnValue
    public GrpcRequest<Member> getMember(final long guildId, final long userId) throws GrpcRequestException {
        return getMember(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcRequest<Member> getMember(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("member", botId, guildId, userId);
            final Member member = context.getContextObject(ctxKey);
            if (member != null) {
                return new FinishedRequestImpl<>(member);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildMemberResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasMember()) {
                    return null;
                }
                final MemberData data = response.getMember();
                final Member member = new Member(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, member);
                }
                return member;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildMember(GetGuildMemberRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse)));
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
        return listGuildMembersAfter(botId, guildId, 0L);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembersAfter(final long botId, final long guildId, final long after) throws GrpcRequestException {
        final int requestLimit = 500;
        return new ContinuationGrpcRequestImpl<>(
                listGuildMembersAfter(botId, guildId, after, requestLimit),
                members -> listGuildMembersAfter(botId, guildId, members == null ? 0 : members.stream()
                        .mapToLong(Member::getId)
                        .max()
                        .orElse(0), requestLimit),
                (oldValue, newValue) -> oldValue == null || newValue.size() - requestLimit >= oldValue.size(),
                (listGrpcRequest, members) -> listGrpcRequest.transform(newMembers -> {
                    final List<Member> copy = new ArrayList<>(members);
                    copy.addAll(newMembers);
                    return copy;
                })
        );
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> listGuildMembersAfter(final long botId, final long guildId, final long after,
                                                           final int limit) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<ListGuildMembersResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getMembersList().stream()
                    .map(member -> new Member(gatewayGrpcClient, botId, member))
                    .collect(Collectors.toList()), () ->
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .run(() -> client.listGuildMembers(ListGuildMembersRequest.newBuilder()
                                    .setAfter(after)
                                    .setLimit(limit)
                                    .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // - Find (1 Overload)
    @CheckReturnValue
    public GrpcRequest<List<Member>> findMembers(final long guildId, final FindGuildMembersRequest request) throws GrpcRequestException {
        return findMembers(getBotId(), guildId, request);
    }

    @CheckReturnValue
    public GrpcRequest<List<Member>> findMembers(final long botId, final long guildId,
                                                 final FindGuildMembersRequest request) throws GrpcRequestException {
        try {
            final CompletableFutureStreamObserver<FindGuildMembersResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getMembersList().stream()
                    .map(member -> new Member(gatewayGrpcClient, botId, member))
                    .collect(Collectors.toList()), () ->
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .run(() -> client.findGuildMembers(request, asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("role", botId, guildId, roleId);
            final Role role = context.getContextObject(ctxKey);
            if (role != null) {
                return new FinishedRequestImpl<>(role);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildRoleResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasRole()) {
                    return null;
                }
                final RoleData data = response.getRole();
                final Role role = new Role(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, role);
                }
                return role;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildRole(GetGuildRoleRequest.newBuilder()
                            .setRoleId(roleId)
                            .build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("list_roles", botId, guildId);
            final List<Role> roleList = context.getContextObject(ctxKey);
            if (roleList != null) {
                return new FinishedRequestImpl<>(roleList);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<ListGuildRolesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                final List<Role> roleList = response.getRolesList().stream()
                        .map(role -> new Role(gatewayGrpcClient, botId, role))
                        .collect(Collectors.toList());
                if (context != null) {
                    context.populateContext(ctxKey, roleList);
                }
                return roleList;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildRoles(ListGuildRolesRequest.newBuilder().build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("emoji", botId, guildId, emojiId);
            final Emoji emoji = context.getContextObject(ctxKey);
            if (emoji != null) {
                return new FinishedRequestImpl<>(emoji);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildEmojiResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasEmoji()) {
                    return null;
                }
                final EmojiData data = response.getEmoji();
                final Emoji emoji = new Emoji(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, emoji);
                }
                return emoji;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                            .setEmojiId(emojiId)
                            .build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("list_emojis", botId, guildId);
            final List<Emoji> emojiList = context.getContextObject(ctxKey);
            if (emojiList != null) {
                return new FinishedRequestImpl<>(emojiList);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<ListGuildEmojisResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                final List<Emoji> emojiList = response.getEmojisList().stream()
                        .map(emoji -> new Emoji(gatewayGrpcClient, botId, emoji))
                        .collect(Collectors.toList());
                if (context != null) {
                    context.populateContext(ctxKey, emojiList);
                }
                return emojiList;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildEmojis(ListGuildEmojisRequest.newBuilder().build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Users (1x + 1 Overload)
    @CheckReturnValue
    public GrpcRequest<User> getUser(final long guildId, final long userId) throws GrpcRequestException {
        return getUser(getBotId(), guildId, userId);
    }

    @CheckReturnValue
    public GrpcRequest<User> getUser(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("user", botId, userId);
            final User user = context.getContextObject(ctxKey);
            if (user != null) {
                return new FinishedRequestImpl<>(user);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetUserResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasUser()) {
                    return null;
                }
                final UserData data = response.getUser();
                final User user = new User(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, user);
                }
                return user;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getUser(GetUserRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("voice_state", botId, guildId, userId);
            final MemberVoiceState voiceState = context.getContextObject(ctxKey);
            if (voiceState != null) {
                return new FinishedRequestImpl<>(voiceState);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<GetGuildMemberVoiceStateResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> {
                if (!response.hasVoiceStateData()) {
                    return null;
                }
                final VoiceStateData data = response.getVoiceStateData();
                final MemberVoiceState voiceState = new MemberVoiceState(gatewayGrpcClient, botId, data);
                if (context != null) {
                    context.populateContext(ctxKey, voiceState);
                }
                return voiceState;
            }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.getGuildMemberVoiceState(GetGuildMemberVoiceStateRequest.newBuilder()
                            .setUserId(userId)
                            .build(), asyncResponse)));
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
        final EventContext context = EventContext.current();
        final String ctxKey;
        if (context != null) {
            ctxKey = EventContext.buildContextKey("list_voice_states", botId, guildId);
            final List<MemberVoiceState> voiceStateList = context.getContextObject(ctxKey);
            if (voiceStateList != null) {
                return new FinishedRequestImpl<>(voiceStateList);
            }
        } else {
            ctxKey = null;
        }
        try {
            final CompletableFutureStreamObserver<ListGuildChannelVoiceStatesResponse> asyncResponse =
                    new CompletableFutureStreamObserver<>();

            return new GrpcRequestImpl<>(executorService, asyncResponse,
                    response -> {
                        final List<MemberVoiceState> voiceStateList = response.getVoiceStatesDataList().stream()
                                .map(voiceStateData -> new MemberVoiceState(gatewayGrpcClient, botId, voiceStateData))
                                .collect(Collectors.toList());
                        if (context != null) {
                            context.populateContext(ctxKey, voiceStateList);
                        }
                        return voiceStateList;
                    }, () -> Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                    .run(() -> client.listGuildChannelVoiceStates(ListGuildChannelVoiceStatesRequest.newBuilder()
                            .setChannelId(channelId)
                            .build(), asyncResponse)));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }
}
