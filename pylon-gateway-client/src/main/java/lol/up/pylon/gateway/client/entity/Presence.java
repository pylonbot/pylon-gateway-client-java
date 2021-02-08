package lol.up.pylon.gateway.client.entity;

import bot.pylon.proto.discord.v1.model.PresenceData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;

import java.util.List;
import java.util.stream.Collectors;

public class Presence implements Entity<PresenceData> {

    private final GatewayGrpcClient grpcClient;
    private final long botId;
    private final PresenceData data;

    public Presence(final GatewayGrpcClient grpcClient, final long botId, final PresenceData data) {
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
        return data.getGuildId();
    }

    @Override
    public PresenceData getData() {
        return data;
    }

    // DATA

    public PresenceData.OnlineStatus getOnlineStatus() {
        return getData().getStatus();
    }

    public List<Activity> getActivities() {
        return getData().getActivitiesList().stream()
                .map(activityData -> new Activity(this, activityData))
                .collect(Collectors.toList());
    }

    public static class Activity {

        private final Presence presence;
        private final PresenceData.PresenceActivityData data;

        private Activity(final Presence presence, final PresenceData.PresenceActivityData data) {
            this.presence = presence;
            this.data = data;
        }

        public Presence getPresence() {
            return presence;
        }

        public PresenceData.PresenceActivityData getData() {
            return data;
        }

        public PresenceData.PresenceActivityData.ActivityType getType() {
            return getData().getType();
        }

        public String getName() {
            return getData().getName();
        }
    }
}
