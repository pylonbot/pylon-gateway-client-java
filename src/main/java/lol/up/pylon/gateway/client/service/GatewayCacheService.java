package lol.up.pylon.gateway.client.service;

import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import rpc.gateway.v1.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;

public class GatewayCacheService {

    private final GatewayCacheGrpc.GatewayCacheBlockingStub client;
    private final GatewayGrpcClient gatewayGrpcClient;

    public GatewayCacheService(final GatewayGrpcClient gatewayGrpcClient,
                               final GatewayCacheGrpc.GatewayCacheBlockingStub client) {
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

    @Nullable
    public Guild getGuild(final long guildId) throws GrpcRequestException {
        return getGuild(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    @Nullable
    public Guild getGuild(final long botId, final long guildId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildResponse response = client.getGuild(GetGuildRequest.newBuilder().build());
                return response.hasGuild() ? response.getGuild() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuild gRPC", throwable);
        }
    }

    @Nullable
    public Channel getChannel(final long guildId, final long channelId) throws GrpcRequestException {
        return getChannel(gatewayGrpcClient.getDefaultBotId(), guildId, channelId);
    }

    @Nullable
    public Channel getChannel(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildChannelResponse response = client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                        .setChannelId(channelId)
                        .build());
                return response.hasChannel() ? response.getChannel() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getChannel gRPC", throwable);
        }
    }

    @Nullable
    public Member getMember(final long guildId, final long userId) throws GrpcRequestException {
        return getMember(gatewayGrpcClient.getDefaultBotId(), guildId, userId);
    }

    @Nullable
    public Member getMember(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildMemberResponse response = client.getGuildMember(GetGuildMemberRequest.newBuilder()
                        .setUserId(userId)
                        .build());
                return response.hasMember() ? response.getMember() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getMember gRPC", throwable);
        }
    }

    @Nullable
    public Role getRole(final long guildId, final long roleId) throws GrpcRequestException {
        return getRole(gatewayGrpcClient.getDefaultBotId(), guildId, roleId);
    }

    @Nullable
    public Role getRole(final long botId, final long guildId, final long roleId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildRoleResponse response = client.getGuildRole(GetGuildRoleRequest.newBuilder()
                        .setRoleId(roleId)
                        .build());
                return response.hasRole() ? response.getRole() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getRole gRPC", throwable);
        }
    }

    @Nullable
    public Emoji getEmoji(final long guildId, final long emojiId) throws GrpcRequestException {
        return getEmoji(gatewayGrpcClient.getDefaultBotId(), guildId, emojiId);
    }

    @Nullable
    public Emoji getEmoji(final long botId, final long guildId, final long emojiId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildEmojiResponse response = client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                        .setEmojiId(emojiId)
                        .build());
                return response.hasEmoji() ? response.getEmoji() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getEmoji gRPC", throwable);
        }
    }

    @Nullable
    public VoiceStateData getVoiceState(final long guildId, final long userId) throws GrpcRequestException {
        return getVoiceState(gatewayGrpcClient.getDefaultBotId(), guildId, userId);
    }

    @Nullable
    public VoiceStateData getVoiceState(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildMemberVoiceStateResponse response = client.getGuildMemberVoiceState(
                        GetGuildMemberVoiceStateRequest.newBuilder()
                                .setUserId(userId)
                                .build());
                return response.hasVoiceStateData() ? response.getVoiceStateData() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getVoiceStateData gRPC", throwable);
        }
    }

    @Nullable
    public User getUser(final long botId, final long userId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L).call(() -> {
                final GetUserResponse response = client.getUser(
                        GetUserRequest.newBuilder()
                                .setUserId(userId)
                                .build());
                return response.hasUser() ? response.getUser() : null;
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getVoiceStateData gRPC", throwable);
        }
    }

    public List<Channel> listGuildChannels(final long guildId) throws GrpcRequestException {
        return listGuildChannels(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<Channel> listGuildChannels(final long botId, final long guildId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildChannelsResponse response = client.listGuildChannels(
                        ListGuildChannelsRequest.newBuilder().build());
                return response.getChannelsList();
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listGuildChannels gRPC", throwable);
        }
    }

    public List<Member> listGuildMembers(final long guildId) throws GrpcRequestException {
        return listGuildMembers(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<Member> listGuildMembersAfter(final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(gatewayGrpcClient.getDefaultBotId(), guildId, after);
    }

    public List<Member> listGuildMembersAfter(final long guildId, long after, int limit) throws GrpcRequestException {
        return listGuildMembersAfter(gatewayGrpcClient.getDefaultBotId(), guildId, after, limit);
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
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildMembersResponse response = client.listGuildMembers(ListGuildMembersRequest.newBuilder()
                        .setAfter(after)
                        .setLimit(limit)
                        .build());
                return response.getMembersList();
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listGuildMembers gRPC", throwable);
        }
    }

    public List<Role> listGuildRoles(final long guildId) throws GrpcRequestException {
        return listGuildRoles(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<Role> listGuildRoles(final long botId, final long guildId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildRolesResponse response = client.listGuildRoles(
                        ListGuildRolesRequest.newBuilder().build());
                return response.getRolesList();
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listGuildRoles gRPC", throwable);
        }
    }

    public List<Emoji> listGuildEmojis(final long guildId) throws GrpcRequestException {
        return listGuildEmojis(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<Emoji> listGuildEmojis(final long botId, final long guildId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildEmojisResponse response = client.listGuildEmojis(
                        ListGuildEmojisRequest.newBuilder().build());
                return response.getEmojisList();
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildEmojis gRPC", throwable);
        }
    }

    public List<Webhook> listChannelWebhooks(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelWebhooks(gatewayGrpcClient.getDefaultBotId(), guildId, channelId);
    }

    public List<Webhook> listChannelWebhooks(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListTextChannelWebhooksResponse response = client.listTextChannelWebhooks(
                        ListTextChannelWebhooksRequest.newBuilder()
                                .setChannelId(channelId)
                                .build());
                return response.getWebhooksList();
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listChannelWebhooks gRPC", throwable);
        }
    }

    public List<VoiceStateData> listChannelVoiceStates(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelVoiceStates(gatewayGrpcClient.getDefaultBotId(), guildId, channelId);
    }

    public List<VoiceStateData> listChannelVoiceStates(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            return Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildChannelVoiceStatesResponse response = client.listGuildChannelVoiceStates(
                        ListGuildChannelVoiceStatesRequest.newBuilder()
                                .setChannelId(channelId)
                                .build());
                return response.getVoiceStatesDataList();
            });
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listChannelVoiceStates gRPC", throwable);
        }
    }
}
