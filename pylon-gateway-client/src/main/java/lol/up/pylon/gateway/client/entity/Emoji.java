package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.EmojiData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.List;

public class Emoji implements Entity<EmojiData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private EmojiData data;

    public Emoji(final GatewayGrpcClient grpcClient, final long botId, final EmojiData data) {
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
        return getData().getGuildId();
    }

    @Override
    public EmojiData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public String getName() {
        return getData().getName();
    }

    public boolean isAnimated() {
        return getData().getAnimated();
    }

    public List<Long> getRoleIds() {
        return getData().getRolesList();
    }

    public boolean isManaged() {
        return getData().getManaged();
    }

    // DATA UTIL

    public String getAsMention() {
        if (isAnimated()) {
            return "<a:" + getName() + ":" + getId() + ">";
        } else {
            return "<:" + getName() + ":" + getId() + ">";
        }
    }

    // REST

    @CheckReturnValue
    public GrpcRequest<Void> changeName(final String name) {
        return changeName(name, null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> changeName(final String name, @Nullable final String reason) {
        return getClient().getRestService().modifyGuildEmoji(getBotId(), getGuildId(), getData().getId(), name, reason)
                .transform(emoji -> {
                    this.data = emoji.getData();
                    return null;
                });
    }

    @CheckReturnValue
    public GrpcRequest<Void> delete() {
        return delete(null);
    }

    @CheckReturnValue
    public GrpcRequest<Void> delete(@Nullable final String reason) {
        return getClient().getRestService().deleteGuildEmoji(getBotId(), getGuildId(), getData().getId(), reason);
    }


}
