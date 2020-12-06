package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.EmojiData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

import javax.annotation.Nullable;

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

    public void changeName(final String name) {
        changeName(name, null);
    }

    public void changeName(final String name, @Nullable final String reason) {
        this.data = getClient().getRestService().modifyGuildEmoji(getBotId(), getGuildId(), getData().getId(), name, reason)
                .getData();
    }

    public void delete() {
        delete(null);
    }

    public void delete(@Nullable final String reason) {
        getClient().getRestService().deleteGuildEmoji(getBotId(), getGuildId(), getData().getId(), reason);
    }

}
