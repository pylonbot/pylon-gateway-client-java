package lol.up.pylon.gateway.client.service.request;

import lol.up.pylon.gateway.client.exception.GrpcException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class GrpcRequestImpl<T> implements GrpcRequest<T> {

    private static final Logger log = LoggerFactory.getLogger(GrpcRequest.class);
    protected static final Runnable COMPLETED = () -> {};

    protected final CompletableFuture<T> future;
    protected final Executor executor;
    protected final GrpcException source;
    protected final Runnable request;
    private boolean submitted = false;

    public <V> GrpcRequestImpl(final Executor executor, final CompletableFuture<V> future,
                               final Function<V, T> transformer, final Runnable request) {
        this.executor = executor;
        this.future = future.thenApply(transformer);
        this.source = new GrpcException("Source trace");
        this.request = request;
    }

    public GrpcRequestImpl(final Executor executor, final CompletableFuture<T> future) {
        this.executor = executor;
        this.future = future;
        this.source = new GrpcException("Source trace");
        this.request = COMPLETED;
    }

    public GrpcRequestImpl(final Executor executor, final CompletableFuture<T> future, final Runnable request,
                           final GrpcException source) {
        this.executor = executor;
        this.future = future;
        this.source = source;
        this.request = request;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    @Override
    public CompletableFuture<T> submit() {
        if (submitted) {
            throw new GrpcRequestException("This request already has been submitted and cannot submit twice!");
        }
        submitted = true;
        request.run();
        return future;
    }

    @Override
    public <V> GrpcRequest<V> transform(Function<T, V> transformer) {
        return new GrpcRequestImpl<>(executor, getFuture()
                .thenApply(transformer), request, source);
    }

    @Override
    public <V> GrpcRequest<V> flatTransform(Function<T, GrpcRequest<V>> transformer) {
        final CompletableFuture<V> transformed = getFuture()
                .thenApplyAsync(transformer, executor)
                .thenComposeAsync(GrpcRequest::getFuture, executor);
        return new GrpcRequestImpl<>(executor, transformed, request, source);
    }

    @Override
    public <V, P> GrpcRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer) {
        final CompletableFuture<V> transformed = getFuture()
                .thenCombineAsync(other.getFuture(), transformer, executor);
        return new GrpcRequestImpl<>(executor, transformed, request, source);
    }

    @Override
    public void queue(final Consumer<? super T> success, final Consumer<? super Throwable> error) {
        submit().thenAcceptAsync(result -> {
            try {
                success.accept(result);
            } catch (final Exception ex) {
                log.error("An error occurred in callback", ex);
            }
        }, executor).exceptionally(throwable -> {
            try {
                error.accept(throwable);
            } catch (final Exception ex) {
                log.error("An error occurred in callback", ex);
            }
            return null;
        });
    }

    @Override
    public T complete() {
        try {
            return submit().get();
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable, source);
        }
    }
}
