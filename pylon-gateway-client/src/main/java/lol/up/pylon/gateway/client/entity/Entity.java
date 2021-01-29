package lol.up.pylon.gateway.client.entity;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;

public interface Entity<E> {

    GatewayGrpcClient getClient();
    long getBotId();
    long getGuildId();
    E getData();

    default GrpcRequest<Guild> getGuild() {
        return getClient().getCacheService().getGuild(getBotId(), getGuildId());
    }
}
