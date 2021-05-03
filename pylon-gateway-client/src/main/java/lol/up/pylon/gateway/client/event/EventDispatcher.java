package lol.up.pylon.gateway.client.event;

import bot.pylon.proto.discord.v1.event.EventEnvelope;
import lol.up.pylon.gateway.client.entity.event.Event;

public interface EventDispatcher {

    <E extends Event<E>> void registerReceiver(final Class<E> eventClass, final AbstractEventReceiver<E> receiver);

    void dispatchEvent(final EventEnvelope.HeaderData headerData, Event<? extends Event> event);

}
