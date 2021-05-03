package lol.up.pylon.gateway.client.event;

import bot.pylon.proto.discord.v1.event.EventEnvelope;
import lol.up.pylon.gateway.client.entity.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class DefaultEventDispatcher implements EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    private final ExecutorService executor;
    private final ScheduledExecutorService asyncExecutor;
    private final Map<Class<? extends Event<?>>, List<AbstractEventReceiver<? extends Event<?>>>> receiverHolder;

    public DefaultEventDispatcher(final ExecutorService executor, final ScheduledExecutorService asyncExecutor) {
        this.executor = new EventExecutorService(executor, EventContext.localContext());
        this.asyncExecutor = new ScheduledEventExecutorService(asyncExecutor, EventContext.localContext());
        this.receiverHolder = new ConcurrentHashMap<>();
    }

    public <E extends Event<E>> void registerReceiver(final Class<E> eventClass,
                                                      final AbstractEventReceiver<E> receiver) {
        final List<AbstractEventReceiver<? extends Event<?>>> receivers =
                receiverHolder.computeIfAbsent(eventClass, k -> new ArrayList<>());
        receivers.add(receiver);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void dispatchEvent(final EventEnvelope.HeaderData headerData, Event<? extends Event> event) {
        log.trace("Dispatching event {} from guild {} on bot {}",
                event.getClass().getSimpleName(), event.getGuildId(), event.getBotId());
        final Class<? extends Event> interfaceType = event.getInterfaceType();
        final List<AbstractEventReceiver<? extends Event<?>>> receivers = receiverHolder.get(interfaceType);
        if (receivers == null) {
            return;
        }
        executor.submit(() -> {
            try {
                final EventContext context = new EventContext(asyncExecutor, event.getBotId(), event.getGuildId());
                EventContext.localContext().set(context);
                receivers.forEach(receiver -> {
                    try {
                        ((AbstractEventReceiver) receiver).receive(headerData, event);
                    } catch (final Throwable throwable) {
                        log.error("An error occurred in event-receiver {}",
                                receiver.getClass().getCanonicalName(), throwable);
                    }
                });
                EventContext.localContext().set(null);
            } catch (final Throwable throwable) {
                log.error("An error occurred when dispatching event {}", event, throwable);
            }
        });
    }
}
