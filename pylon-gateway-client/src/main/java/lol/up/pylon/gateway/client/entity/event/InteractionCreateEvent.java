package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.Member;
import lol.up.pylon.gateway.client.entity.User;

import java.util.Optional;

public interface InteractionCreateEvent extends Event<InteractionCreateEvent> {

    default MessageData.MessageInteractionData.MessageInteractionType getType() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return event.getType();
    }

    default PingEvent getPingEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return new PingEvent(getBotId(), event.getPing());
    }

    default ApplicationCommandEvent getApplicationCommandEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return new ApplicationCommandEvent(getBotId(), event.getCommand());
    }

    default MessageComponentEvent getComponentEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return new MessageComponentEvent(getBotId(), event.getComponent());
    }

    default InteractionCreateBase getBaseEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        switch (getType()) {
            case PING:
                return getPingEvent();
            case APPLICATION_COMMAND:
                return getApplicationCommandEvent();
            case MESSAGE_COMPONENT:
                return getComponentEvent();
        }
        throw new IllegalStateException("Received unknown event type");
    }

    default String getToken() {
        return getBaseEvent().getBase().getToken();
    }

    default long getApplicationId() {
        return getBaseEvent().getBase().getApplicationId();
    }

    default long getInteractionId() {
        return getBaseEvent().getBase().getId();
    }

    abstract class InteractionCreateBase {

        protected final long botId;

        protected InteractionCreateBase(final long botId) {
            this.botId = botId;
        }

        protected abstract bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateBasePayload getBase();

        public boolean isFromGuild() {
            return getBase().getSourceType() == bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateBasePayloadType.GUILD;
        }

        public User getUser() {
            if (isFromGuild()) {
                return new User(GatewayGrpcClient.getSingleton(), botId,
                        getBase().getSourceGuild().getMember().getUser());
            }
            return new User(GatewayGrpcClient.getSingleton(), botId, getBase().getSourceDm().getUser());
        }

        public Member getMember() {
            if (!isFromGuild()) {
                throw new IllegalArgumentException("Can't access member in a dm interaction");
            }
            return new Member(GatewayGrpcClient.getSingleton(), botId, getBase().getSourceGuild().getMember());
        }

        public Optional<Long> getGuildId() {
            if (isFromGuild()) {
                return Optional.of(getBase().getSourceGuild().getGuildId());
            }
            return Optional.empty();
        }
    }

    class PingEvent extends InteractionCreateBase {

        private final bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreatePingEvent pingEvent;

        PingEvent(final long botId,
                  final bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreatePingEvent pingEvent) {
            super(botId);
            this.pingEvent = pingEvent;
        }

        @Override
        protected bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateBasePayload getBase() {
            return pingEvent.getBase();
        }
    }

    class ApplicationCommandEvent extends InteractionCreateBase {

        private final bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateApplicationCommandEvent applicationCommandEvent;

        ApplicationCommandEvent(final long botId,
                                final bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateApplicationCommandEvent applicationCommandEvent) {
            super(botId);
            this.applicationCommandEvent = applicationCommandEvent;
        }

        @Override
        protected bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateBasePayload getBase() {
            return applicationCommandEvent.getBase();
        }
    }

    class MessageComponentEvent extends InteractionCreateBase {

        private final bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateMessageComponentEvent componentEvent;

        MessageComponentEvent(final long botId,
                              final bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateMessageComponentEvent componentEvent) {
            super(botId);
            this.componentEvent = componentEvent;
        }

        @Override
        protected bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateBasePayload getBase() {
            return componentEvent.getBase();
        }
    }
}
