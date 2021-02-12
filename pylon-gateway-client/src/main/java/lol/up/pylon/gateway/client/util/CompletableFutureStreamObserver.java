package lol.up.pylon.gateway.client.util;

import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.exception.GrpcException;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureStreamObserver<T> extends CompletableFuture<T> implements StreamObserver<T> {

    private boolean completed = false;

    private final ThreadLocal<EventContext> threadLocal;
    private final EventContext eventContext;
    private final GrpcException source;

    public CompletableFutureStreamObserver() {
        threadLocal = EventContext.localContext();
        eventContext = threadLocal.get();
        this.source = new GrpcException("Source trace");
    }

    @Override
    public void onNext(T entity) {
        completeWithContext(() -> complete(entity));
    }

    @Override
    public void onError(Throwable throwable) {
        completeWithContext(() -> completeExceptionally(ExceptionUtil.asGrpcException(throwable, source)));
    }

    @Override
    public void onCompleted() {
        if (!completed) {
            onError(new IllegalStateException("Future didn't receive any next or error signal but completed"));
        }
    }

    private void completeWithContext(final Runnable run) {
        completed = true;
        final EventContext previous = threadLocal.get();
        threadLocal.set(this.eventContext);
        run.run();
        threadLocal.set(previous);
    }
}
