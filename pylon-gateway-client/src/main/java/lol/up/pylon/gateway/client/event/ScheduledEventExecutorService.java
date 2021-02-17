package lol.up.pylon.gateway.client.event;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledEventExecutorService extends EventExecutorService implements ScheduledExecutorService {

    public ScheduledEventExecutorService(final ScheduledExecutorService executorService,
                                         final ThreadLocal<EventContext> localContext) {
        super(executorService, localContext);
    }

    private ScheduledExecutorService executorService() {
        return (ScheduledExecutorService) executorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executorService().schedule(new EventContextRunnable(command, localContext), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executorService().schedule(new EventContextCallable<>(callable, localContext), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executorService().scheduleAtFixedRate(new EventContextRunnable(command, localContext), initialDelay,
                period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executorService().scheduleWithFixedDelay(new EventContextRunnable(command, localContext), initialDelay,
                delay, unit);
    }
}
