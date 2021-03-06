package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.MessageData;
import bot.pylon.proto.discord.v1.rest.EditMessageRequest;
import com.google.protobuf.Timestamp;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.exception.InsufficientPermissionException;
import lol.up.pylon.gateway.client.exception.ValidationException;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcApiRequest;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.util.TimeUtil;

import javax.annotation.CheckReturnValue;

public class Message implements Entity<MessageData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private MessageData data;

    public Message(final GatewayGrpcClient grpcClient, final long botId, final MessageData data) {
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
        if (data.getGuildId().isInitialized()) {
            return data.getGuildId().getValue();
        }
        return 0L;
    }

    @Override
    @CheckReturnValue
    public GrpcRequest<Guild> getGuild() {
        if (data.getGuildId().isInitialized()) {
            return getClient().getCacheService().getGuild(getBotId(), getGuildId());
        }
        return new FinishedRequestImpl<>(null);
    }

    @Override
    public MessageData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public long getChannelId() {
        return getData().getChannelId();
    }

    public String getContent() {
        return getData().getContent();
    }

    public User getAuthor() {
        return new User(getClient(), getBotId(), getData().getAuthor());
    }

    public Member getMember() {
        return new Member(getClient(), getBotId(), getData().getMember());
    }

    public boolean isEdited() {
        return getData().hasEditedTimestamp();
    }

    public long getTimeCreated() {
        return TimeUtil.timestampToLong(getData().getTimestamp());
    }

    public long getTimeEdited() {
        final Timestamp timestamp = getData().getEditedTimestamp();
        if (timestamp == null) {
            return 0;
        }
        return TimeUtil.timestampToLong(timestamp);
    }


    // REST

    @CheckReturnValue
    public GrpcApiRequest<Void> addReaction(final Emoji emoji) {
        return addReaction(emoji.getName() + ":" + emoji.getId());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> addReaction(final String emoji) {
        if (getGuildId() > 0) {
            final Member member = getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId())
                    .complete();
            if (!member.hasPermission(Permission.ADD_REACTIONS)) {
                throw new InsufficientPermissionException(Permission.ADD_REACTIONS);
            }
        }
        return getClient().getRestService().createReaction(getBotId(), getGuildId(), getChannelId(), getId(), emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeReaction(final Emoji emoji) {
        return removeReaction(emoji.getName() + ":" + emoji.getId());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeReaction(final String emoji) {
        return getClient().getRestService().deleteOwnReaction(getBotId(), getGuildId(), getChannelId(), getId(), emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeReaction(final long userId, final Emoji emoji) {
        return removeReaction(userId, emoji.getName() + ":" + emoji.getId());
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> removeReaction(final long userId, final String emoji) {
        if (getGuildId() > 0) {
            final Member member = getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId())
                    .complete();
            if (!member.hasPermission(Permission.MANAGE_MESSAGES)) {
                throw new InsufficientPermissionException(Permission.MANAGE_MESSAGES);
            }
        }
        return getClient().getRestService().deleteReaction(getBotId(), getGuildId(), getChannelId(), getId(), userId,
                emoji);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> editMessage(final String message) {
        if (getAuthor().getId() != getBotId()) {
            throw new ValidationException("Can't edit messages from other users");
        }
        return getClient().getRestService().editMessage(getBotId(), getGuildId(), EditMessageRequest.newBuilder()
                .setChannelId(getChannelId())
                .setMessageId(getId())
                .setContent(message)
                .build()).transform(msg -> {
            this.data = msg.getData();
            return null;
        });
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> editMessage(final MessageData.MessageEmbedData embed) {
        if (getAuthor().getId() != getBotId()) {
            throw new ValidationException("Can't edit messages from other users");
        }
        return getClient().getRestService().editMessage(getBotId(), getGuildId(), EditMessageRequest.newBuilder()
                .setChannelId(getChannelId())
                .setMessageId(getId())
                .setEmbed(embed)
                .build()).transform(msg -> {
            this.data = msg.getData();
            return null;
        });
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> delete() {
        return delete(null);
    }

    @CheckReturnValue
    public GrpcApiRequest<Void> delete(final String reason) {
        if (getGuildId() > 0 && getAuthor().getId() != getBotId()) {
            final Member member = getClient().getCacheService().getMember(getBotId(), getGuildId(), getBotId())
                    .complete();
            if (!member.hasPermission(Permission.MANAGE_MESSAGES)) {
                throw new InsufficientPermissionException(Permission.MANAGE_MESSAGES);
            }
        }
        return getClient().getRestService().deleteMessage(getBotId(), getGuildId(), getChannelId(), getId(), reason);
    }

    // CACHE

    @CheckReturnValue
    public GrpcRequest<Channel> getChannel() {
        if (getGuildId() > 0) {
            return getClient().getCacheService().getChannel(getBotId(), getGuildId(), getChannelId());
        } else {
            return getClient().getCacheService().getDmChannel(getBotId(), getChannelId(), getAuthor().getId());
        }
    }
}
