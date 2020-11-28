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
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.util.ExceptionUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class CacheService {

    private static CacheService instance;

    public static CacheService getSingleton() {
        return instance;
    }

    private final GatewayCacheGrpc.GatewayCacheBlockingStub client;
    private final GatewayGrpcClient gatewayGrpcClient;

    public CacheService(final GatewayGrpcClient gatewayGrpcClient,
                        final GatewayCacheGrpc.GatewayCacheBlockingStub client) {
        if (instance != null) {
            throw new IllegalStateException("You might only create GatewayCacheService once");
        }
        instance = this;
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
        return gatewayGrpcClient.getDefaultBotId();
    }

    // Guilds (1x + Overload)
    @Nullable
    public Guild getGuild(final long guildId) throws GrpcRequestException {
        return getGuild(getBotId(), guildId);
    }

    @Nullable
    public Guild getGuild(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final GuildData data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildResponse response = client.getGuild(GetGuildRequest.newBuilder().build());
                return response.hasGuild() ? response.getGuild() : null;
            });
            if (data == null) {
                return null;
            }
            return new Guild(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // Channels (2x + Overloads)
    // - Get (1 Overload)
    @Nullable
    public Channel getChannel(final long guildId, final long channelId) throws GrpcRequestException {
        return getChannel(getBotId(), guildId, channelId);
    }

    @Nullable
    public Channel getChannel(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            final ChannelData data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildChannelResponse response = client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                        .setChannelId(channelId)
                        .build());
                return response.hasChannel() ? response.getChannel() : null;
            });
            if (data == null) {
                return null;
            }
            return new Channel(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // - List (1 Overload)
    public List<Channel> listGuildChannels(final long guildId) throws GrpcRequestException {
        return listGuildChannels(getBotId(), guildId);
    }

    public List<Channel> listGuildChannels(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final List<ChannelData> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildChannelsResponse response = client.listGuildChannels(
                        ListGuildChannelsRequest.newBuilder().build());
                return response.getChannelsList();
            });
            return dataList.stream()
                    .map(channel -> new Channel(this, botId, channel))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }


    // Guild Members (2x + Overloads)
    // - Get (1 Overload)
    @Nullable
    public Member getMember(final long guildId, final long userId) throws GrpcRequestException {
        return getMember(getBotId(), guildId, userId);
    }

    @Nullable
    public Member getMember(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final MemberData data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildMemberResponse response = client.getGuildMember(GetGuildMemberRequest.newBuilder()
                        .setUserId(userId)
                        .build());
                return response.hasMember() ? response.getMember() : null;
            });
            if (data == null) {
                return null;
            }
            return new Member(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // - List (5 Overloads)
    public List<Member> listGuildMembers(final long guildId) throws GrpcRequestException {
        return listGuildMembers(getBotId(), guildId);
    }

    public List<Member> listGuildMembersAfter(final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(getBotId(), guildId, after);
    }

    public List<Member> listGuildMembersAfter(final long guildId, long after, int limit) throws GrpcRequestException {
        return listGuildMembersAfter(getBotId(), guildId, after, limit);
    }

    public List<Member> listGuildMembers(final long botId, final long guildId) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, 0);
    }

    public List<Member> listGuildMembersAfter(final long botId, final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, after, 0);
    }

    public List<Member> listGuildMembersAfter(final long botId, final long guildId, final long after,
                                              final int limit) throws GrpcRequestException {
        try {
            final List<MemberData> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildMembersResponse response = client.listGuildMembers(ListGuildMembersRequest.newBuilder()
                        .setAfter(after)
                        .setLimit(limit)
                        .build());
                return response.getMembersList();
            });
            return dataList.stream()
                    .map(member -> new Member(this, botId, member))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // Guild Member Properties (2x + Overloads)
    // - Get ( 1 Overload)
    @Nullable
    public Role getRole(final long guildId, final long roleId) throws GrpcRequestException {
        return getRole(getBotId(), guildId, roleId);
    }

    @Nullable
    public Role getRole(final long botId, final long guildId, final long roleId) throws GrpcRequestException {
        try {
            final RoleData data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildRoleResponse response = client.getGuildRole(GetGuildRoleRequest.newBuilder()
                        .setRoleId(roleId)
                        .build());
                return response.hasRole() ? response.getRole() : null;
            });
            if (data == null) {
                return null;
            }
            return new Role(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // - List (1 Overload)
    public List<Role> listGuildRoles(final long guildId) throws GrpcRequestException {
        return listGuildRoles(getBotId(), guildId);
    }

    public List<Role> listGuildRoles(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final List<RoleData> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildRolesResponse response = client.listGuildRoles(
                        ListGuildRolesRequest.newBuilder().build());
                return response.getRolesList();
            });
            return dataList.stream()
                    .map(role -> new Role(this, botId, role))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // Emojis (2x + Overloads)
    // - Get (1 Overload)
    @Nullable
    public Emoji getEmoji(final long guildId, final long emojiId) throws GrpcRequestException {
        return getEmoji(getBotId(), guildId, emojiId);
    }

    @Nullable
    public Emoji getEmoji(final long botId, final long guildId, final long emojiId) throws GrpcRequestException {
        try {
            final EmojiData data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildEmojiResponse response = client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                        .setEmojiId(emojiId)
                        .build());
                return response.hasEmoji() ? response.getEmoji() : null;
            });
            if (data == null) {
                return null;
            }
            return new Emoji(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // - List (1 Overload)
    public List<Emoji> listGuildEmojis(final long guildId) throws GrpcRequestException {
        return listGuildEmojis(getBotId(), guildId);
    }

    public List<Emoji> listGuildEmojis(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final List<EmojiData> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID
                    , guildId).call(() -> {
                final ListGuildEmojisResponse response = client.listGuildEmojis(
                        ListGuildEmojisRequest.newBuilder().build());
                return response.getEmojisList();
            });
            return dataList.stream()
                    .map(emoji -> new Emoji(this, botId, emoji))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // Users (1x + 1 Overload)
    @Nullable
    public User getUser(final long userId) throws GrpcRequestException {
        return getUser(getBotId(), userId);
    }

    @Nullable
    public User getUser(final long botId, final long userId) throws GrpcRequestException {
        try {
            final UserData data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                    .call(() -> {
                        final GetUserResponse response = client.getUser(
                                GetUserRequest.newBuilder()
                                        .setUserId(userId)
                                        .build());
                        return response.hasUser() ? response.getUser() : null;
                    });
            if (data == null) {
                return null;
            }
            return new User(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // VoiceStates (2x + Overloads)
    // - Get (Overload)
    @Nullable
    public MemberVoiceState getVoiceState(final long guildId, final long userId) throws GrpcRequestException {
        return getVoiceState(getBotId(), guildId, userId);
    }

    @Nullable
    public MemberVoiceState getVoiceState(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final VoiceStateData data = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildMemberVoiceStateResponse response = client.getGuildMemberVoiceState(
                        GetGuildMemberVoiceStateRequest.newBuilder()
                                .setUserId(userId)
                                .build());
                return response.hasVoiceStateData() ? response.getVoiceStateData() : null;
            });
            if (data == null) {
                return null;
            }
            return new MemberVoiceState(this, botId, data);
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    // - List (1 Overload)
    public List<MemberVoiceState> listChannelVoiceStates(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelVoiceStates(getBotId(), guildId, channelId);
    }

    public List<MemberVoiceState> listChannelVoiceStates(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            final List<VoiceStateData> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildChannelVoiceStatesResponse response = client.listGuildChannelVoiceStates(
                        ListGuildChannelVoiceStatesRequest.newBuilder()
                                .setChannelId(channelId)
                                .build());
                return response.getVoiceStatesDataList();
            });
            return dataList.stream()
                    .map(voiceStateData -> new MemberVoiceState(this, botId, voiceStateData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }
}
