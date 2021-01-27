package lol.up.pylon.gateway.client.service;

import bot.pylon.proto.discord.v1.cache.*;
import bot.pylon.proto.discord.v1.model.*;
import bot.pylon.proto.gateway.v1.service.GatewayCacheGrpc;
import com.google.common.util.concurrent.ListenableFuture;
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
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);

    private final GatewayCacheGrpc.GatewayCacheFutureStub client;
    private final GatewayGrpcClient gatewayGrpcClient;
    private final ExecutorService executorService;

    public CacheService(final GatewayGrpcClient gatewayGrpcClient,
                        final GatewayCacheGrpc.GatewayCacheFutureStub client,
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
    public GrpcRequest<Guild> getGuild(final long guildId) throws GrpcRequestException {
        return getGuild(getBotId(), guildId);
    }

    public GrpcRequest<Guild> getGuild(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetGuildResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuild(GetGuildRequest.newBuilder().build()));
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

    // Channels (2x + Overloads)
    // - Get (1 Overload)
    public GrpcRequest<Channel> getChannel(final long guildId, final long channelId) throws GrpcRequestException {
        return getChannel(getBotId(), guildId, channelId);
    }

    public GrpcRequest<Channel> getChannel(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetGuildChannelResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                                    .setChannelId(channelId)
                                    .build()));
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
    public GrpcRequest<List<Channel>> listGuildChannels(final long guildId) throws GrpcRequestException {
        return listGuildChannels(getBotId(), guildId);
    }

    public GrpcRequest<List<Channel>> listGuildChannels(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final ListenableFuture<ListGuildChannelsResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.listGuildChannels(ListGuildChannelsRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getChannelsList().stream()
                    .map(channel -> new Channel(gatewayGrpcClient, botId, channel))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }


    // Guild Members (2x + Overloads)
    // - Get (1 Overload)
    public GrpcRequest<Member> getMember(final long guildId, final long userId) throws GrpcRequestException {
        return getMember(getBotId(), guildId, userId);
    }

    public GrpcRequest<Member> getMember(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetGuildMemberResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildMember(GetGuildMemberRequest.newBuilder()
                                    .setUserId(userId)
                                    .build()));
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
    public GrpcRequest<List<Member>> listGuildMembers(final long guildId) throws GrpcRequestException {
        return listGuildMembers(getBotId(), guildId);
    }

    public GrpcRequest<List<Member>> listGuildMembersAfter(final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(getBotId(), guildId, after);
    }

    public GrpcRequest<List<Member>> listGuildMembersAfter(final long guildId, long after, int limit) throws GrpcRequestException {
        return listGuildMembersAfter(getBotId(), guildId, after, limit);
    }

    public GrpcRequest<List<Member>> listGuildMembers(final long botId, final long guildId) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, 0);
    }

    public GrpcRequest<List<Member>> listGuildMembersAfter(final long botId, final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, after, 0);
    }

    public GrpcRequest<List<Member>> listGuildMembersAfter(final long botId, final long guildId, final long after,
                                                           final int limit) throws GrpcRequestException {
        try {
            final ListenableFuture<ListGuildMembersResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.listGuildMembers(ListGuildMembersRequest.newBuilder()
                                    .setAfter(after)
                                    .setLimit(limit)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getMembersList().stream()
                    .map(member -> new Member(gatewayGrpcClient, botId, member))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Guild Member Properties (2x + Overloads)
    // - Get ( 1 Overload)
    public GrpcRequest<Role> getRole(final long guildId, final long roleId) throws GrpcRequestException {
        return getRole(getBotId(), guildId, roleId);
    }

    public GrpcRequest<Role> getRole(final long botId, final long guildId, final long roleId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetGuildRoleResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildRole(GetGuildRoleRequest.newBuilder()
                                    .setRoleId(roleId)
                                    .build()));
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
    public GrpcRequest<List<Role>> listGuildRoles(final long guildId) throws GrpcRequestException {
        return listGuildRoles(getBotId(), guildId);
    }

    public GrpcRequest<List<Role>> listGuildRoles(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final ListenableFuture<ListGuildRolesResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.listGuildRoles(ListGuildRolesRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getRolesList().stream()
                    .map(role -> new Role(gatewayGrpcClient, botId, role))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Emojis (2x + Overloads)
    // - Get (1 Overload)
    public GrpcRequest<Emoji> getEmoji(final long guildId, final long emojiId) throws GrpcRequestException {
        return getEmoji(getBotId(), guildId, emojiId);
    }

    public GrpcRequest<Emoji> getEmoji(final long botId, final long guildId, final long emojiId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetGuildEmojiResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                                    .setEmojiId(emojiId)
                                    .build()));
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
    public GrpcRequest<List<Emoji>> listGuildEmojis(final long guildId) throws GrpcRequestException {
        return listGuildEmojis(getBotId(), guildId);
    }

    public GrpcRequest<List<Emoji>> listGuildEmojis(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final ListenableFuture<ListGuildEmojisResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.listGuildEmojis(ListGuildEmojisRequest.newBuilder().build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse, response -> response.getEmojisList().stream()
                    .map(emoji -> new Emoji(gatewayGrpcClient, botId, emoji))
                    .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }

    // Users (1x + 1 Overload)
    public GrpcRequest<User> getUser(final long userId) throws GrpcRequestException {
        return getUser(getBotId(), userId);
    }

    public GrpcRequest<User> getUser(final long botId, final long userId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetUserResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                            .call(() -> client.getUser(GetUserRequest.newBuilder()
                                    .setUserId(userId)
                                    .build()));
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
    public GrpcRequest<MemberVoiceState> getVoiceState(final long guildId, final long userId) throws GrpcRequestException {
        return getVoiceState(getBotId(), guildId, userId);
    }

    public GrpcRequest<MemberVoiceState> getVoiceState(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final ListenableFuture<GetGuildMemberVoiceStateResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.getGuildMemberVoiceState(GetGuildMemberVoiceStateRequest.newBuilder()
                                    .setUserId(userId)
                                    .build()));
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
    public GrpcRequest<List<MemberVoiceState>> listChannelVoiceStates(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelVoiceStates(getBotId(), guildId, channelId);
    }

    public GrpcRequest<List<MemberVoiceState>> listChannelVoiceStates(final long botId, final long guildId,
                                                                      final long channelId) throws GrpcRequestException {
        try {
            final ListenableFuture<ListGuildChannelVoiceStatesResponse> asyncResponse =
                    Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId)
                            .call(() -> client.listGuildChannelVoiceStates(ListGuildChannelVoiceStatesRequest.newBuilder()
                                    .setChannelId(channelId)
                                    .build()));
            return new GrpcRequestImpl<>(executorService, asyncResponse,
                    response -> response.getVoiceStatesDataList().stream()
                            .map(voiceStateData -> new MemberVoiceState(gatewayGrpcClient, botId, voiceStateData))
                            .collect(Collectors.toList()));
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }
}
