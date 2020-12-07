package lol.up.pylon.gateway.client.event;

import lol.up.pylon.gateway.client.entity.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

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
        log.trace("Dispatching event {}", event);
        final Class<? extends Event> interfaceType = event.getInterfaceType();
        final List<AbstractEventReceiver<? extends Event<?>>> receivers = receiverHolder.get(interfaceType);
        if (receivers == null) {
            return;
        }
        executor.submit(() -> {
            try {
                EventContext.localContext().set(new EventContext(executor, event.getBotId(), event.getGuildId()));
                receivers.forEach(receiver -> {
                    try {
                        ((AbstractEventReceiver) receiver).receive(event);
                    } catch (final Throwable throwable) {
                        log.error("An error occurred in event-receiver {}",
                                receiver.getClass().getCanonicalName(), throwable);
                    }
                });
            } catch (final Throwable throwable) {
                log.error("An error occurred when dispatching event {}", event, throwable);
            }
        });
    }
}
