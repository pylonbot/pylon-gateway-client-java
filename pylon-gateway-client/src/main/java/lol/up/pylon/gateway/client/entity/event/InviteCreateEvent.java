package lol.up.pylon.gateway.client.entity.event;

import bot.pylon.proto.discord.v1.model.InviteData;

public interface InviteCreateEvent extends Event<InviteCreateEvent> {

    default InviteData getInvite() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.InviteCreateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.InviteCreateEvent event =
                (bot.pylon.proto.discord.v1.event.InviteCreateEvent) this;
        return event.getPayload();
    }

}
