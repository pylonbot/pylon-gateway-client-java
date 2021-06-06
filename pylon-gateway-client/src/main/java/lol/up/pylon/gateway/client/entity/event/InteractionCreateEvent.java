package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.MessageData;

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

    default bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreatePingEvent getPingEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return event.getPing();
    }

    default bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateApplicationCommandEvent getApplicationCommandEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return event.getCommand();
    }

    default bot.pylon.proto.discord.v1.event.InteractionCreateEvent.InteractionCreateMessageComponentEvent getComponentEvent() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InteractionCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InteractionCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InteractionCreateEvent) this;
        return event.getComponent();
    }
}
