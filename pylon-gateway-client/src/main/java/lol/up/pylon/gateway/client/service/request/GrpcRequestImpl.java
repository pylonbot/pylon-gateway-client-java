package lol.up.pylon.gateway.client.service.request;

import com.google.common.util.concurrent.ListenableFuture;
import lol.up.pylon.gateway.client.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public class GrpcRequestImpl<T> implements GrpcRequest<T> {

    private static final Logger log = LoggerFactory.getLogger(GrpcRequestImpl.class);

    private static final Consumer<Object> SUCCESS = o -> {};
    private static final Consumer<Throwable> ERROR = error -> log.error("An error occurred in grpc request", error);

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

    private final ListenableFuture<?> future;
    private final Function<Object, T> transformer;
    private final Executor executor;

    public <V> GrpcRequestImpl(final ExecutorService executor, final ListenableFuture<V> future,
                               final Function<V, T> transformer) {
        this.executor = executor;
        this.future = future;
        //noinspection unchecked
        this.transformer = (Function<Object, T>) transformer;
    }

    private GrpcRequestImpl(final Executor executor, final ListenableFuture<?> future, final Function<?, T> transformer) {
        this.executor = executor;
        this.future = future;
        //noinspection unchecked
        this.transformer = (Function<Object, T>) transformer;
    }

    public <V> GrpcRequestImpl<V> transform(Function<T, V> transformer) {
        return new GrpcRequestImpl<>(executor, future, this.transformer.andThen(transformer));
    }

    public void queue() {
        queue(DEFAULT_SUCCESS_HANDLER);
    }

    public void queue(final Consumer<? super T> success) {
        queue(success, DEFAULT_ERROR_HANDLER);
    }

    public void queue(final Consumer<? super T> success, final Consumer<Throwable> error) {
        future.addListener(() -> {
            try {
                final Object response = future.get();
                final T entity = transformer.apply(response);
                success.accept(entity);
            } catch (final Throwable throwable) {
                error.accept(throwable);
            }
        }, executor);
    }

    public T complete() {
        try {
            final Object response = future.get();
            return transformer.apply(response);
        } catch (final Throwable throwable) {
            throw ExceptionUtil.asGrpcException(throwable);
        }
    }
}
