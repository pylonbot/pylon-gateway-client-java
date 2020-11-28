package lol.up.pylon.gateway.client.service;

import bot.pylon.proto.discord.v1.model.GuildBanData;
import bot.pylon.proto.discord.v1.model.InviteData;
import bot.pylon.proto.discord.v1.model.MessageData;
import bot.pylon.proto.discord.v1.rest.*;
import bot.pylon.proto.gateway.v1.service.GatewayRestGrpc;
import com.google.protobuf.ByteString;
import io.grpc.CallCredentials;
import io.grpc.Context;
import io.grpc.Metadata;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.*;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.exception.GrpcGatewayApiException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.util.ExceptionUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class RestService {

    private static RestService instance;

    public static RestService getSingleton() {
        return instance;
    }

    private final GatewayRestGrpc.GatewayRestBlockingStub client;
    private final GatewayGrpcClient gatewayGrpcClient;

    public RestService(final GatewayGrpcClient gatewayGrpcClient,
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

    private String getErrorMessage(final RestError apiError) {
        return "An error occurred during REST request: " +
                "HTTPStatus:" + apiError.getStatus() + " | " +
                "ErrorCode:" + apiError.getCode() + " | " +
                "Message:" + apiError.getMessage();
    }

    public Guild modifyGuild(final long botId, final long guildId, final ModifyGuildRequest request) throws GrpcRequestException, GrpcGatewayApiException {
        try {
            final ModifyGuildResponse response = Context.current().withValues(Constants.CTX_BOT_ID, botId,
                    Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuild(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Guild(gatewayGrpcClient.getCacheService(), botId, response.getData().getGuild());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
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
            return new Channel(gatewayGrpcClient.getCacheService(), botId, response.getData().getChannel());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            return response.getData().getAdded();
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return false; // unreachable
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public List<GuildBanData> getGuildBans(final long botId, final long guildId, final GetGuildBansRequest request) throws GrpcRequestException {
        try {
            final GetGuildBansResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildBans(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getBansList();
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    @Nullable
    public GuildBanData getGuildBan(final long botId, final long guildId, final GetGuildBanRequest request) throws GrpcRequestException {
        try {
            final GetGuildBanResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildBan(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            if (!response.hasData() || !response.getData().hasBan()) {
                return null;
            }
            return response.getData().getBan();
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            return new Role(gatewayGrpcClient.getCacheService(), botId, response.getData().getRole());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
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
            return response.getData().getRolesList().stream()
                    .map(roleData -> new Role(gatewayGrpcClient.getCacheService(), botId, roleData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
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
            return new Role(gatewayGrpcClient.getCacheService(), botId, response.getData().getRole());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            return response.getData().getSerializedSize(); // todo ehhhhh?
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return 0; // unreachable
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
            ExceptionUtil.rethrowGrpcException(throwable);
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
            return response.getData().getRegionsList().asByteStringList().stream()
                    .map(ByteString::toStringUtf8)
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public List<InviteData> getGuildInvites(final long botId, final long guildId,
                                            final GetGuildInvitesRequest request) throws GrpcRequestException {
        try {
            final GetGuildInvitesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildInvites(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getInvitesList(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public Channel modifyChannel(final long botId, final long guildId, final ModifyChannelRequest request) throws GrpcRequestException {
        try {
            final ModifyChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Channel(gatewayGrpcClient.getCacheService(), botId, response.getData().getChannel());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void deleteChannel(final long botId, final long guildId, final DeleteChannelRequest request) throws GrpcRequestException {
        try {
            final DeleteChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public MessageData createMessage(final long botId, final long guildId, final CreateMessageRequest request) throws GrpcRequestException {
        try {
            final CreateMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getMessage(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public MessageData crosspostMessage(final long botId, final long guildId, final CrosspostMessageRequest request) throws GrpcRequestException {
        try {
            final CrosspostMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.crosspostMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getMessage(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void createReaction(final long botId, final long guildId, final CreateReactionRequest request) throws GrpcRequestException {
        try {
            final CreateReactionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createReaction(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void deleteOwnReaction(final long botId, final long guildId, final DeleteOwnReactionRequest request) throws GrpcRequestException {
        try {
            final DeleteOwnReactionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteOwnReaction(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void deleteReaction(final long botId, final long guildId, final DeleteUserReactionRequest request) throws GrpcRequestException {
        try {
            final DeleteUserReactionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteUserReaction(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void deleteAllReactions(final long botId, final long guildId, final DeleteAllReactionsRequest request) throws GrpcRequestException {
        try {
            final DeleteAllReactionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteAllReactions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void deleteEmoteReactions(final long botId, final long guildId,
                                     final DeleteAllReactionsForEmojiRequest request) throws GrpcRequestException {
        try {
            final DeleteAllReactionsForEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteAllReactionsForEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public MessageData editMessage(final long botId, final long guildId, final EditMessageRequest request) throws GrpcRequestException {
        try {
            final EditMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.editMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getMessage(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void deleteMessage(final long botId, final long guildId, final DeleteMessageRequest request) throws GrpcRequestException {
        try {
            final DeleteMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void bulkDeleteMessages(final long botId, final long guildId, final BulkDeleteMessagesRequest request) throws GrpcRequestException {
        try {
            final BulkDeleteMessagesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.bulkDeleteMessages(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void editChannelPermissions(final long botId, final long guildId,
                                       final EditChannelPermissionsRequest request) throws GrpcRequestException {
        try {
            final EditChannelPermissionsResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.editChannelPermissions(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public List<InviteData> getChannelInvites(final long botId, final long guildId,
                                              final GetChannelInvitesRequest request) throws GrpcRequestException {
        try {
            final GetChannelInvitesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getChannelInvites(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getInvitesList(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public InviteData createChannelInvite(final long botId, final long guildId,
                                          final CreateChannelInviteRequest request) throws GrpcRequestException {
        try {
            final CreateChannelInviteResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createChannelInvite(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getInvite(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void deleteChannelPermission(final long botId, final long guildId,
                                        final DeleteChannelPermissionRequest request) throws GrpcRequestException {
        try {
            final DeleteChannelPermissionResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteChannelPermission(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public long followNewsChannel(final long botId, final long guildId, final FollowNewsChannelRequest request) throws GrpcRequestException {
        try {
            final FollowNewsChannelResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.followNewsChannel(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getChannelId();
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return 0L; // unreachable
        }
    }

    public void startTyping(final long botId, final long guildId, final TriggerTypingIndicatorRequest request) throws GrpcRequestException {
        try {
            final TriggerTypingIndicatorResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.triggerTypingIndicator(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public List<MessageData> getPinnedMessages(final long botId, final long guildId,
                                               final GetPinnedMessagesRequest request) throws GrpcRequestException {
        try {
            final GetPinnedMessagesResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getPinnedMessages(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getMessagesList(); // todo wrap nicely
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void pinMessage(final long botId, final long guildId, final AddPinnedChannelMessageRequest request) throws GrpcRequestException {
        try {
            final AddPinnedChannelMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.addPinnedChannelMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public void unpinMessage(final long botId, final long guildId, final DeletePinnedChannelMessageRequest request) throws GrpcRequestException {
        try {
            final DeletePinnedChannelMessageResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deletePinnedChannelMessage(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public List<Emoji> listGuildEmojis(final long botId, final long guildId,
                                       final ListGuildEmojisRequest request) throws GrpcRequestException {
        try {
            final ListGuildEmojisResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.listGuildEmojis(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return response.getData().getEmojisList().stream()
                    .map(emojiData -> new Emoji(gatewayGrpcClient.getCacheService(), botId, emojiData))
                    .collect(Collectors.toList());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public Emoji getGuildEmoji(final long botId, final long guildId, final GetGuildEmojiRequest request) throws GrpcRequestException {
        try {
            final GetGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Emoji(gatewayGrpcClient.getCacheService(), botId, response.getData().getEmoji());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public Emoji createGuildEmoji(final long botId, final long guildId, final CreateGuildEmojiRequest request) throws GrpcRequestException {
        try {
            final CreateGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Emoji(gatewayGrpcClient.getCacheService(), botId, response.getData().getEmoji());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public Emoji modifyGuildEmoji(final long botId, final long guildId, final ModifyGuildEmojiRequest request) throws GrpcRequestException {
        try {
            final ModifyGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Emoji(gatewayGrpcClient.getCacheService(), botId, response.getData().getEmoji());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void deleteGuildEmoji(final long botId, final long guildId, final DeleteGuildEmojiRequest request) throws GrpcRequestException {
        try {
            final DeleteGuildEmojiResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.deleteGuildEmoji(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public User getSelfUser(final long botId, final long guildId, final GetCurrentUserRequest request) throws GrpcRequestException {
        try {
            final GetCurrentUserResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getCurrentUser(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new User(gatewayGrpcClient.getCacheService(), botId, response.getData().getUser());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public User getUser(final long botId, final long guildId, final GetUserRequest request) throws GrpcRequestException {
        try {
            final GetUserResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.getUser(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new User(gatewayGrpcClient.getCacheService(), botId, response.getData().getUser());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public User modifySelfUser(final long botId, final long guildId, final ModifyCurrentUserRequest request) throws GrpcRequestException {
        try {
            final ModifyCurrentUserResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.modifyCurrentUser(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new User(gatewayGrpcClient.getCacheService(), botId, response.getData().getUser());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

    public void leaveGuild(final long botId, final long guildId, final LeaveGuildRequest request) throws GrpcRequestException {
        try {
            final LeaveGuildResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.leaveGuild(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
        }
    }

    public Channel createDmChannel(final long botId, final long guildId, final CreateDmRequest request) throws GrpcRequestException {
        try {
            final CreateDmResponse response = Context.current().withValues(Constants.CTX_BOT_ID,
                    botId, Constants.CTX_GUILD_ID, guildId)
                    .call(() -> client.createDm(request));
            if (response.hasError()) {
                throw new GrpcGatewayApiException(response.getError(), getErrorMessage(response.getError()));
            }
            return new Channel(gatewayGrpcClient.getCacheService(), botId, response.getData().getChannel());
        } catch (final Throwable throwable) {
            ExceptionUtil.rethrowGrpcException(throwable);
            return null; // unreachable
        }
    }

}
