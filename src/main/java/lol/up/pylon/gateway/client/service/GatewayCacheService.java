package lol.up.pylon.gateway.client.service;

import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.*;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import rpc.gateway.v1.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
    public GuildWrapper getGuild(final long guildId) throws GrpcRequestException {
        return getGuild(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    @Nullable
    public GuildWrapper getGuild(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final Guild data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildResponse response = client.getGuild(GetGuildRequest.newBuilder().build());
                return response.hasGuild() ? response.getGuild() : null;
            });
            return new GuildWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuild gRPC", throwable);
        }
    }

    @Nullable
    public ChannelWrapper getChannel(final long guildId, final long channelId) throws GrpcRequestException {
        return getChannel(gatewayGrpcClient.getDefaultBotId(), guildId, channelId);
    }

    @Nullable
    public ChannelWrapper getChannel(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            final Channel data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildChannelResponse response = client.getGuildChannel(GetGuildChannelRequest.newBuilder()
                        .setChannelId(channelId)
                        .build());
                return response.hasChannel() ? response.getChannel() : null;
            });
            return new ChannelWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getChannel gRPC", throwable);
        }
    }

    @Nullable
    public MemberWrapper getMember(final long guildId, final long userId) throws GrpcRequestException {
        return getMember(gatewayGrpcClient.getDefaultBotId(), guildId, userId);
    }

    @Nullable
    public MemberWrapper getMember(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final Member data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildMemberResponse response = client.getGuildMember(GetGuildMemberRequest.newBuilder()
                        .setUserId(userId)
                        .build());
                return response.hasMember() ? response.getMember() : null;
            });
            return new MemberWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getMember gRPC", throwable);
        }
    }

    @Nullable
    public RoleWrapper getRole(final long guildId, final long roleId) throws GrpcRequestException {
        return getRole(gatewayGrpcClient.getDefaultBotId(), guildId, roleId);
    }

    @Nullable
    public RoleWrapper getRole(final long botId, final long guildId, final long roleId) throws GrpcRequestException {
        try {
            final Role data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildRoleResponse response = client.getGuildRole(GetGuildRoleRequest.newBuilder()
                        .setRoleId(roleId)
                        .build());
                return response.hasRole() ? response.getRole() : null;
            });
            return new RoleWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getRole gRPC", throwable);
        }
    }

    @Nullable
    public EmojiWrapper getEmoji(final long guildId, final long emojiId) throws GrpcRequestException {
        return getEmoji(gatewayGrpcClient.getDefaultBotId(), guildId, emojiId);
    }

    @Nullable
    public EmojiWrapper getEmoji(final long botId, final long guildId, final long emojiId) throws GrpcRequestException {
        try {
            final Emoji data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID,
                    guildId).call(() -> {
                final GetGuildEmojiResponse response = client.getGuildEmoji(GetGuildEmojiRequest.newBuilder()
                        .setEmojiId(emojiId)
                        .build());
                return response.hasEmoji() ? response.getEmoji() : null;
            });
            return new EmojiWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getEmoji gRPC", throwable);
        }
    }

    @Nullable
    public VoiceStateWrapper getVoiceState(final long guildId, final long userId) throws GrpcRequestException {
        return getVoiceState(gatewayGrpcClient.getDefaultBotId(), guildId, userId);
    }

    @Nullable
    public VoiceStateWrapper getVoiceState(final long botId, final long guildId, final long userId) throws GrpcRequestException {
        try {
            final VoiceStateData data = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final GetGuildMemberVoiceStateResponse response = client.getGuildMemberVoiceState(
                        GetGuildMemberVoiceStateRequest.newBuilder()
                                .setUserId(userId)
                                .build());
                return response.hasVoiceStateData() ? response.getVoiceStateData() : null;
            });
            return new VoiceStateWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getVoiceStateData gRPC", throwable);
        }
    }

    @Nullable
    public UserWrapper getUser(final long botId, final long userId) throws GrpcRequestException {
        try {
            final User data = Context.current().withValues(Constants.CTX_BOT_ID, botId, Constants.CTX_GUILD_ID, 0L)
                    .call(() -> {
                        final GetUserResponse response = client.getUser(
                                GetUserRequest.newBuilder()
                                        .setUserId(userId)
                                        .build());
                        return response.hasUser() ? response.getUser() : null;
                    });
            return new UserWrapper(this, botId, data);
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getVoiceStateData gRPC", throwable);
        }
    }

    public List<ChannelWrapper> listGuildChannels(final long guildId) throws GrpcRequestException {
        return listGuildChannels(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<ChannelWrapper> listGuildChannels(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final List<Channel> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildChannelsResponse response = client.listGuildChannels(
                        ListGuildChannelsRequest.newBuilder().build());
                return response.getChannelsList();
            });
            return dataList.stream()
                    .map(channel -> new ChannelWrapper(this, botId, channel))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listGuildChannels gRPC", throwable);
        }
    }

    public List<MemberWrapper> listGuildMembers(final long guildId) throws GrpcRequestException {
        return listGuildMembers(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<MemberWrapper> listGuildMembersAfter(final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(gatewayGrpcClient.getDefaultBotId(), guildId, after);
    }

    public List<MemberWrapper> listGuildMembersAfter(final long guildId, long after, int limit) throws GrpcRequestException {
        return listGuildMembersAfter(gatewayGrpcClient.getDefaultBotId(), guildId, after, limit);
    }

    public List<MemberWrapper> listGuildMembers(final long botId, final long guildId) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, 0);
    }

    public List<MemberWrapper> listGuildMembersAfter(final long botId, final long guildId, final long after) throws GrpcRequestException {
        return listGuildMembersAfter(botId, guildId, after, 0);
    }

    public List<MemberWrapper> listGuildMembersAfter(final long botId, final long guildId, final long after,
                                                     final int limit) throws GrpcRequestException {
        try {
            final List<Member> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildMembersResponse response = client.listGuildMembers(ListGuildMembersRequest.newBuilder()
                        .setAfter(after)
                        .setLimit(limit)
                        .build());
                return response.getMembersList();
            });
            return dataList.stream()
                    .map(member -> new MemberWrapper(this, botId, member))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listGuildMembers gRPC", throwable);
        }
    }

    public List<RoleWrapper> listGuildRoles(final long guildId) throws GrpcRequestException {
        return listGuildRoles(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<RoleWrapper> listGuildRoles(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final List<Role> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListGuildRolesResponse response = client.listGuildRoles(
                        ListGuildRolesRequest.newBuilder().build());
                return response.getRolesList();
            });
            return dataList.stream()
                    .map(role -> new RoleWrapper(this, botId, role))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listGuildRoles gRPC", throwable);
        }
    }

    public List<EmojiWrapper> listGuildEmojis(final long guildId) throws GrpcRequestException {
        return listGuildEmojis(gatewayGrpcClient.getDefaultBotId(), guildId);
    }

    public List<EmojiWrapper> listGuildEmojis(final long botId, final long guildId) throws GrpcRequestException {
        try {
            final List<Emoji> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID
                    , guildId).call(() -> {
                final ListGuildEmojisResponse response = client.listGuildEmojis(
                        ListGuildEmojisRequest.newBuilder().build());
                return response.getEmojisList();
            });
            return dataList.stream()
                    .map(emoji -> new EmojiWrapper(this, botId, emoji))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildEmojis gRPC", throwable);
        }
    }

    public List<WebhookWrapper> listChannelWebhooks(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelWebhooks(gatewayGrpcClient.getDefaultBotId(), guildId, channelId);
    }

    public List<WebhookWrapper> listChannelWebhooks(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
        try {
            final List<Webhook> dataList = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId).call(() -> {
                final ListTextChannelWebhooksResponse response = client.listTextChannelWebhooks(
                        ListTextChannelWebhooksRequest.newBuilder()
                                .setChannelId(channelId)
                                .build());
                return response.getWebhooksList();
            });
            return dataList.stream()
                    .map(webhook -> new WebhookWrapper(this, botId, webhook))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listChannelWebhooks gRPC", throwable);
        }
    }

    public List<VoiceStateWrapper> listChannelVoiceStates(final long guildId, final long channelId) throws GrpcRequestException {
        return listChannelVoiceStates(gatewayGrpcClient.getDefaultBotId(), guildId, channelId);
    }

    public List<VoiceStateWrapper> listChannelVoiceStates(final long botId, final long guildId, final long channelId) throws GrpcRequestException {
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
                    .map(voiceStateData -> new VoiceStateWrapper(this, botId, voiceStateData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during listChannelVoiceStates gRPC", throwable);
        }
    }
}
