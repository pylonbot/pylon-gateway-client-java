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
            throw new GrpcRequestException("An error occurred during getGuildChannels gRPC", throwable);
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
            throw new GrpcRequestException("An error occurred during getGuildMembers gRPC", throwable);
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
            throw new GrpcRequestException("An error occurred during getGuildRoles gRPC", throwable);
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
}
