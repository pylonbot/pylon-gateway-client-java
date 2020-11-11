package lol.up.pylon.gateway.client.service;

import com.google.protobuf.ByteString;
import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Channel;
import lol.up.pylon.gateway.client.entity.Role;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.exception.GrpcGatewayApiException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import pylon.rpc.discord.v1.api.*;
import pylon.rpc.discord.v1.model.GuildBan;
import pylon.rpc.discord.v1.model.GuildData;
import pylon.rpc.discord.v1.model.InviteData;
import pylon.rpc.gateway.v1.rest.GatewayRestGrpc;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class ApiService {

    private static ApiService instance;

    public static ApiService getSingleton() {
        return instance;
    }

    private final GatewayRestGrpc.GatewayRestBlockingStub client;
    private final GatewayGrpcClient gatewayGrpcClient;

    public ApiService(final GatewayGrpcClient gatewayGrpcClient,
                      final GatewayRestGrpc.GatewayRestBlockingStub client) {
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

    private String getErrorMessage(final ApiError apiError) {
        return "An error occurred during REST request: " +
                "HTTPStatus:" + apiError.getStatus() + " | " +
                "ErrorCode:" + apiError.getCode() + " | " +
                "Message:" + apiError.getMessage();
    }

    public GuildData modifyGuild(final long botId, final long guildId, final ModifyGuildRequest request) throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildResponse response = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuild(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getResponse();
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during modifyGuild gRPC", throwable);
        }
    }

    public Channel createChannel(final long botId, final long guildId, final CreateGuildChannelRequest request) throws GrpcRequestException {
        try {
            final CreateGuildChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Channel(gatewayGrpcClient.getCacheService(), botId, response.getResponse());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during createChannel gRPC", throwable);
        }
    }

    public void modifyChannelPositions(final long botId, final long guildId,
                                       final ModifyGuildChannelPositionsRequest request) throws GrpcRequestException {
        try {
            final ModifyGuildChannelPositionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildChannelPositions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during modifyChannelPositions gRPC", throwable);
        }
    }

    public boolean addGuildMember(final long botId, final long guildId, final AddGuildMemberRequest request) throws GrpcRequestException {
        try {
            final AddGuildMemberResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.addGuildMember(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getAdded();
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during addGuildMember gRPC", throwable);
        }
    }

    public void modifyGuildMember(final long botId, final long guildId, final ModifyGuildMemberRequest request) throws GrpcRequestException {
        try {
            final ModifyGuildMemberResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildMember(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during modifyGuildMember gRPC", throwable);
        }
    }

    public void changeSelfNickname(final long botId, final long guildId, final ModifyCurrentUserNickRequest request) throws GrpcRequestException {
        try {
            final ModifyCurrentUserNickResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyCurrentUserNick(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during changeSelfNickname gRPC", throwable);
        }
    }

    public void addMemberRole(final long botId, final long guildId, final AddGuildMemberRoleRequest request) throws GrpcRequestException {
        try {
            final AddGuildMemberRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.addGuildMemberRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during addMemberRole gRPC", throwable);
        }
    }

    public void removeMemberRole(final long botId, final long guildId, final RemoveGuildMemberRoleRequest request) throws GrpcRequestException {
        try {
            final RemoveGuildMemberRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.removeGuildMemberRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during removeMemberRole gRPC", throwable);
        }
    }

    public void removeGuildMember(final long botId, final long guildId, final RemoveGuildMemberRequest request) throws GrpcRequestException {
        try {
            final RemoveGuildMemberResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.removeGuildMember(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during removeGuildMember gRPC", throwable);
        }
    }

    public List<GuildBan> getGuildBans(final long botId, final long guildId, final GetGuildBansRequest request) throws GrpcRequestException {
        try {
            final GetGuildBansResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildBans(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getBansList();
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildBans gRPC", throwable);
        }
    }

    @Nullable
    public GuildBan getGuildBan(final long botId, final long guildId, final GetGuildBanRequest request) throws GrpcRequestException {
        try {
            final GetGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            if (!response.hasBan()) {
                return null;
            }
            return response.getBan();
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildBan gRPC", throwable);
        }
    }

    public void createGuildBan(final long botId, final long guildId, final CreateGuildBanRequest request) throws GrpcRequestException {
        try {
            final CreateGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during createGuildBan gRPC", throwable);
        }
    }

    public void removeGuildBan(final long botId, final long guildId, final RemoveGuildBanRequest request) throws GrpcRequestException {
        try {
            final RemoveGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.removeGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during removeGuildBan gRPC", throwable);
        }
    }

    public Role createGuildRole(final long botId, final long guildId, final CreateGuildRoleRequest request) throws GrpcRequestException {
        try {
            final CreateGuildRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Role(gatewayGrpcClient.getCacheService(), botId, response.getRole());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during createGuildRole gRPC", throwable);
        }
    }

    public List<Role> modifyGuildRolePositions(final long botId, final long guildId,
                                               final ModifyGuildRolePositionsRequest request) throws GrpcRequestException {
        try {
            final ModifyGuildRolePositionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildRolePositions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getRolesList().stream()
                    .map(roleData -> new Role(gatewayGrpcClient.getCacheService(), botId, roleData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during modifyGuildRolePositions gRPC", throwable);
        }
    }

    public Role modifyGuildRole(final long botId, final long guildId, final ModifyGuildRoleRequest request) throws GrpcRequestException {
        try {
            final ModifyGuildRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Role(gatewayGrpcClient.getCacheService(), botId, response.getRole());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during modifyGuildRole gRPC", throwable);
        }
    }

    public void deleteGuildRole(final long botId, final long guildId, final DeleteGuildRoleRequest request) throws GrpcRequestException {
        try {
            final DeleteGuildRoleResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteGuildRole(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during deleteGuildRole gRPC", throwable);
        }
    }

    public int getGuildPruneCount(final long botId, final long guildId, final GetGuildPruneCountRequest request) throws GrpcRequestException {
        try {
            final GetGuildPruneCountResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildPruneCount(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getSerializedSize(); // todo ehhhhh?
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildPruneCount gRPC", throwable);
        }
    }

    public void beginGuildPrune(final long botId, final long guildId, final BeginGuildPruneRequest request) throws GrpcRequestException {
        try {
            final BeginGuildPruneResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.beginGuildPrune(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during beginGuildPrune gRPC", throwable);
        }
    }

    public List<String> getGuildVoiceRegions(final long botId, final long guildId,
                                             final GetGuildVoiceRegionsRequest request) throws GrpcRequestException {
        try {
            final GetGuildVoiceRegionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildVoiceRegions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getRegionsList().asByteStringList().stream()
                    .map(ByteString::toStringUtf8)
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildVoiceRegions gRPC", throwable);
        }
    }

    public List<InviteData> getGuildVoiceRegions(final long botId, final long guildId,
                                                 final GetGuildInvitesRequest request) throws GrpcRequestException {
        try {
            final GetGuildInvitesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildInvites(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getInvitesList();
        } catch (final Throwable throwable) {
            throw new GrpcRequestException("An error occurred during getGuildVoiceRegions gRPC", throwable);
        }
    }

}
