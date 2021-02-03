package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.UserData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.List;

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
        final StringBuilder padded = new StringBuilder(String.valueOf(getDiscriminator()));
        while (padded.length() < 4) {
            padded.insert(0, "0");
        }
        return padded.toString();
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

    public String getAsMention() {
        return "<@" + getId() + ">";
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

    @CheckReturnValue
    public GrpcRequest<List<Guild>> getMutualGuilds() {
        return new FinishedRequestImpl<>(new ArrayList<>()); // todo implement
    }
}
