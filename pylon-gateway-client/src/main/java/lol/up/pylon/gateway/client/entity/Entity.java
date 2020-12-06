package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.GatewayGrpcClient;

public interface Entity<E> {

    GatewayGrpcClient getClient();
    long getBotId();
    long getGuildId();
    E getData();

    default Guild getGuild() {
        return getClient().getCacheService().getGuild(getBotId(), getGuildId());
    }
}
