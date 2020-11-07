package lol.up.pylon.gateway.client.event;

import lol.up.pylon.gateway.client.entity.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class EventDispatcher {

    private final ExecutorService executor;
    private final Map<Class<? extends Event<?>>, List<AbstractEventReceiver<? extends Event<?>>>> receiverHolder;

    public EventDispatcher(final ExecutorService executor) {
        this.executor = new EventExecutorService(executor, EventContext.localContext());
        this.receiverHolder = new ConcurrentHashMap<>();
    }

    public <E extends Event<E>> void registerReceiver(final Class<E> eventClass,
                                                      final AbstractEventReceiver<E> receiver) {
        final List<AbstractEventReceiver<? extends Event<?>>> receivers =
                receiverHolder.computeIfAbsent(eventClass, k -> new ArrayList<>());
        receivers.add(receiver);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void dispatchEvent(Event<? extends Event> event) {
        executor.submit(() -> {
            EventContext.localContext().set(new EventContext(executor, event.getBotId(), event.getGuildId()));
            final Class<? extends Event> interfaceType = event.getInterfaceType();
            final List<AbstractEventReceiver<? extends Event<?>>> receivers = receiverHolder.get(interfaceType);
            if (receivers == null) {
                return;
            }
            receivers.forEach(receiver -> ((AbstractEventReceiver) receiver).receive(event));
        });
    }
}
