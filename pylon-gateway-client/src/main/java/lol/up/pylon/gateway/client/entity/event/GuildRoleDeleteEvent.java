package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Role;

public interface GuildRoleDeleteEvent extends Event<GuildRoleDeleteEvent> {

    default Role getRole() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildRoleDeleteEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildRoleDeleteEvent event =
                (bot.pylon.proto.discord.v1.event.GuildRoleDeleteEvent) this;
        return new Role(GatewayGrpcClient.getSingleton(), event.getBotId(), event.getPayload());
    }

}
