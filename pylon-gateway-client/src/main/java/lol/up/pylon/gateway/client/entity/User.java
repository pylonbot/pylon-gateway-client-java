package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.UserData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;

public class User implements Entity<UserData> {

    private static String AVATAR_URL = "https://cdn.discordapp.com/avatars/%s/%s.%s";
    private static String DEFAULT_AVATAR_URL = "https://cdn.discordapp.com/embed/avatars/%s.png";

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final UserData data;

    public User(final GatewayGrpcClient grpcClient, final long botId, final UserData data) {
        this.grpcClient = grpcClient;
        this.botId = botId;
        this.data = data;
    }

    @Override
    public GatewayGrpcClient getClient() {
        return grpcClient;
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        throw new RuntimeException("Can't get a guildId on a user object");
    }

    @Override
    public UserData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public boolean isBot() {
        return getData().getBot();
    }

    public String getName() {
        return getData().getUsername();
    }

    public int getDiscriminator() {
        return getData().getDiscriminator();
    }

    public String getPaddedDiscriminator() {
        return String.valueOf(getDiscriminator()); // todo: discrim fix (either format or passed as str)
    }

    public long getUserId() {
        return getData().getId();
    }

    public String getAvatarId() {
        if(getData().getAvatar().isInitialized()) {
            return getData().getAvatar().getValue();
        }
        return null;
    }

    // DATA UTIL

    public String getAsTag() {
        return getName() + "#" + getPaddedDiscriminator();
    }

    public String getAvatarUrl() {
        final String avatarId = getAvatarId();
        if(avatarId == null) {
            final String defaultAvatarId = String.valueOf(getDiscriminator() % 5);
            return String.format(DEFAULT_AVATAR_URL, defaultAvatarId);
        } else {
            return String.format(AVATAR_URL, getId(), avatarId, avatarId.startsWith("a_") ? "gif" : "png");
        }
    }

    // REST

    @CheckReturnValue
    public GrpcRequest<Channel> openPrivateChannel() {
        return getClient().getRestService().createDmChannel(getBotId(), getGuildId(), getId());
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<Member> getAsMember(final Guild guild) {
        return getAsMember(guild.getGuildId());
    }

    @CheckReturnValue
    public GrpcRequest<Member> getAsMember(final long guildId) {
        return getClient().getCacheService().getMember(botId, guildId, getData().getId());
    }
}
