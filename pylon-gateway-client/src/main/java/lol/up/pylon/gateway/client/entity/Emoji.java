package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.EmojiData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.CacheService;

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
    public CacheService getGatewayCacheService() {
        return grpcClient.getCacheService();
    }

    @Override
    public long getBotId() {
        return botId;
    }

    @Override
    public long getGuildId() {
        return data.getGuildId();
    }

    @Override
    public EmojiData getData() {
        return data;
    }

    public void changeName(final String name) {
        changeName(name, null);
    }

    public void changeName(final String name, @Nullable final String reason) {
        this.data = grpcClient.getRestService().modifyGuildEmoji(getBotId(), getGuildId(), data.getId(), name, reason)
                .getData();
    }

    public void delete() {
        delete(null);
    }

    public void delete(@Nullable final String reason) {
        grpcClient.getRestService().deleteGuildEmoji(getBotId(), getGuildId(), data.getId(), reason);
    }

}
