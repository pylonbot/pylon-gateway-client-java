package lol.up.pylon.gateway.client.event;

import bot.pylon.proto.discord.v1.event.EventEnvelope;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.entity.event.Event;
import lol.up.pylon.gateway.client.service.CacheService;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractEventReceiver<E extends Event> {

    /**
     * If you want to spawn an asynchronous thread from this event receiver you have to use this async method
     * If you do not use this async method the new thread will not know about the {@link EventContext}
     * Following this all requests to {@link CacheService} will not
     * know about the related botId, so the {@link CacheService} will
     * instead use the {@link GatewayGrpcClient}'s default botId
     *
     * @param runnable the new task to execute
     * @return an empty future which completes when the task was executed
     */
    public final Future<?> async(final Runnable runnable) {
        return EventContext.current().getExecutorService().submit(runnable);
    }

    /**
     * If you want to spawn an asynchronous thread from this event receiver you have to use this async method
     * If you do not use this async method the new thread will not know about the {@link EventContext}
     * Following this all requests to {@link CacheService} will not
     * know about the related botId, so the {@link CacheService} will
     * instead use the {@link GatewayGrpcClient}'s default botId
     *
     * @param callable the new task to execute
     * @return A future holding the result of the callable as soon as the task completes
     */
    public final <V> Future<V> async(final Callable<V> callable) {
        return EventContext.current().getExecutorService().submit(callable);
    }

    public final ScheduledFuture<?> asyncAfter(Runnable command, long delay, TimeUnit unit) {
        return EventContext.current().getExecutorService().schedule(command, delay, unit);
    }

    public final <V> ScheduledFuture<V> asyncAfter(Callable<V> command, long delay, TimeUnit unit) {
        return EventContext.current().getExecutorService().schedule(command, delay, unit);
    }


    protected void receive(EventEnvelope.HeaderData headerData, E event) {
        receive(event);
    }

    protected void receive(E event) {

    }
}
