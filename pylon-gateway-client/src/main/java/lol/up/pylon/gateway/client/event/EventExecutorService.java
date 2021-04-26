package lol.up.pylon.gateway.client.event;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class EventExecutorService implements ExecutorService {

    protected final ExecutorService executorService;
    protected final ThreadLocal<EventContext> localContext;

    public EventExecutorService(final ExecutorService executorService, final ThreadLocal<EventContext> localContext) {
        this.executorService = executorService;
        this.localContext = localContext;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(new EventContextCallable<>(task, localContext));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(new EventContextRunnable(task, localContext), result);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return executorService.submit(new EventContextRunnable(task, localContext));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(new EventContextRunnable(command, localContext));
    }

    static class EventContextRunnable implements Runnable {

        private final Runnable runnable;
        private final ThreadLocal<EventContext> localContext;
        private final EventContext current;

        public EventContextRunnable(final Runnable runnable, final ThreadLocal<EventContext> localContext) {
            this.runnable = runnable;
            this.localContext = localContext;
            this.current = localContext.get();
        }

        @Override
        public void run() {
            final EventContext previous = localContext.get();
            localContext.set(current);
            try {
                runnable.run();
            } finally {
                localContext.set(previous);
            }
        }
    }

    static class EventContextCallable<V> implements Callable<V> {

        private final Callable<V> callable;
        private final ThreadLocal<EventContext> localContext;
        private final EventContext current;

        public EventContextCallable(final Callable<V> callable, final ThreadLocal<EventContext> localContext) {
            this.callable = callable;
            this.localContext = localContext;
            this.current = localContext.get();
        }

        @Override
        public V call() throws Exception {
            final EventContext previous = localContext.get();
            localContext.set(current);
            try {
                return callable.call();
            } finally {
                localContext.set(previous);
            }
        }
    }
}
