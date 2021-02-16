package lol.up.pylon.gateway.client.util;

import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.exception.GrpcException;
import lol.up.pylon.gateway.client.exception.GrpcGatewayApiRateLimitedException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CompletableFutureStreamObserver<T> extends CompletableFuture<T> implements StreamObserver<T> {

    private boolean completed = false;

    private final ThreadLocal<EventContext> threadLocal;
    private final EventContext eventContext;
    private final GrpcException source;
    private final ScheduledExecutorService scheduler;
    private final Supplier<CompletableFutureStreamObserver<T>> retry;
    private final int retryLimit;
    private int retryIndex = 0;

    public CompletableFutureStreamObserver() {
        this(null, null, new GrpcException("Source trace"));
    }

    public CompletableFutureStreamObserver(final ScheduledExecutorService scheduler,
                                           final Supplier<CompletableFutureStreamObserver<T>> retry,
                                           final GrpcException source) {
        this.threadLocal = EventContext.localContext();
        this.eventContext = threadLocal.get();
        this.source = source;
        this.scheduler = scheduler;
        this.retry = retry;
        this.retryLimit = 0;
    }

    public CompletableFutureStreamObserver(final ScheduledExecutorService scheduler,
                                           final Supplier<CompletableFutureStreamObserver<T>> retry,
                                           final int retryLimit) {
        this.threadLocal = EventContext.localContext();
        this.eventContext = threadLocal.get();
        this.source = new GrpcException("Source trace");
        this.scheduler = scheduler;
        this.retry = retry;
        this.retryLimit = retryLimit;
    }

    @Override
    public void onNext(T entity) {
        completeWithContext(() -> {
            complete(entity);
            return true;
        });
    }

    @Override
    public void onError(Throwable throwable) {
        completeWithContext(() -> {
            final GrpcException grpcException = ExceptionUtil.asGrpcException(throwable, source);
            if (grpcException instanceof GrpcGatewayApiRateLimitedException) {
                final GrpcGatewayApiRateLimitedException exception = (GrpcGatewayApiRateLimitedException) grpcException;
                if (retry != null) {
                    final int retryAt = exception.getApiError().getRateLimited().getRetryAt();
                    if (retryAt > 0) {
                        if (scheduler != null) {
                            if (retryIndex++ < retryLimit) {
                                scheduler.schedule(() -> {
                                    retry.get().thenAccept(this::onNext)
                                            .exceptionally(nestedException -> {
                                                onError(nestedException);
                                                return null;
                                            });
                                }, retryAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                                return false;
                            } else {
                                return true;
                            }
                        }
                    } else {
                        if (retryIndex++ < retryLimit) {
                            retry.get().thenAccept(this::onNext)
                                    .exceptionally(nestedException -> {
                                        onError(nestedException);
                                        return null;
                                    });
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            }
            completeExceptionally(grpcException);
            return true;
        });
    }

    @Override
    public void onCompleted() {
        if (!completed) {
            onError(new IllegalStateException("Future didn't receive any next or error signal but completed"));
        }
    }

    private void completeWithContext(final Supplier<Boolean> complete) {
        completed = true;
        final EventContext previous = threadLocal.get();
        threadLocal.set(this.eventContext);
        completed = complete.get();
        threadLocal.set(previous);
    }
}
