package lol.up.pylon.gateway.client.service.request;

import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class GrpcRequestImpl<T> implements GrpcRequest<T> {

    private static final Logger log = LoggerFactory.getLogger(GrpcRequestImpl.class);

    private static final Consumer<Object> SUCCESS = o -> {};
    private static final Consumer<Throwable> ERROR = error -> log.error("An error occurred during grpc request", error);

    private static Consumer<Object> DEFAULT_SUCCESS_HANDLER = SUCCESS;
    private static Consumer<Throwable> DEFAULT_ERROR_HANDLER = ERROR;

    public static void setDefaultSuccessHandler(final Consumer<Object> success) {
        if (success == null) {
            DEFAULT_SUCCESS_HANDLER = SUCCESS;
        } else {
            DEFAULT_SUCCESS_HANDLER = success;
        }
    }

    public static void setDefaultErrorHandler(final Consumer<Throwable> error) {
        if (error == null) {
            DEFAULT_ERROR_HANDLER = ERROR;
        } else {
            DEFAULT_ERROR_HANDLER = error;
        }
    }

    private final CompletableFuture<T> future;
    private final Executor executor;

    public <V> GrpcRequestImpl(final Executor executor, final CompletableFuture<V> future,
                               final Function<V, T> transformer) {
        this.executor = executor;
        this.future = future.thenApply(transformer);
    }

    public GrpcRequestImpl(final Executor executor, final CompletableFuture<T> future) {
        this.executor = executor;
        this.future = future;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public <V> GrpcRequestImpl<V> transform(Function<T, V> transformer) {
        return new GrpcRequestImpl<>(executor, future.thenApply(transformer));
    }

    public <V, P> GrpcRequestImpl<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer) {
        final CompletableFuture<V> future = getFuture().thenCombineAsync(other.getFuture(), transformer, executor);
        return new GrpcRequestImpl<>(executor, future);
    }

    public void queue() {
        queue(DEFAULT_SUCCESS_HANDLER);
    }

    public void queue(final Consumer<? super T> success) {
        queue(success, DEFAULT_ERROR_HANDLER);
    }

    public void queue(final Consumer<? super T> success, final Consumer<Throwable> error) {
        future.thenAcceptAsync(result -> {
            try {
                success.accept(result);
            } catch (final Exception ex) {
                log.error("An error occurred in callback", ex);
            }
        }, executor).exceptionallyAsync(throwable -> {
            try {
                error.accept(throwable);
            } catch (final Exception ex) {
                log.error("An error occurred in callback", ex);
            }
            return null;
        }, executor);
    }

    public T complete() {
        try {
            return future.get();
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }
}
