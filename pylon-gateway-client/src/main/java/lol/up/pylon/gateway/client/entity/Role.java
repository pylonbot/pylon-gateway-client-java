package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.RoleData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

import java.util.Objects;

public class Role implements Entity<RoleData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final RoleData data;

    public Role(final GatewayGrpcClient grpcClient, final long botId, final RoleData data) {
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
    public RoleData getData() {
        return data;
    }

    // DATA

    public long getId() {
        return getData().getId();
    }

    public int getPosition() {
        return getData().getPosition();
    }

    public long getPermissions() {
        return getData().getPermissions();
    }

    public int getColorRaw() {
        return getData().getColor();
    }

    public boolean isPublicRole() {
        return getGuildId() == getId();
    }

    public String getName() {
        return getData().getName();
    }

    // DATA UTILS

    public String getAsMention() {
        return "<@&" + getId() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return getBotId() == role.getBotId() &&
                getGuildId() == role.getGuildId() &&
                getId() == role.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBotId(), getData());
    }

    @Override
    public String toString() {
        return "Role{" +
                "botId=" + getBotId() +
                ", id=" + getId() +
                ", guildId=" + getGuildId() +
                ", name=" + getName() +
                '}';
    }
}
