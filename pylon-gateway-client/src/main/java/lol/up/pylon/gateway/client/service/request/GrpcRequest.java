package lol.up.pylon.gateway.client.service.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GrpcRequest<T> {

    class Context {

        private static final Logger log = LoggerFactory.getLogger(GrpcRequestImpl.class);

        private static final Consumer<Object> SUCCESS = o -> {};
        private static final Consumer<Throwable> ERROR = error -> log.error("An error occurred during grpc request",
                error);

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
    }

    @CheckReturnValue
    <V> GrpcRequest<V> transform(Function<T, V> transformer);

    @CheckReturnValue
    <V> GrpcRequest<V> flatTransform(Function<T, GrpcRequest<V>> transformer);

    @CheckReturnValue
    <V, P> GrpcRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer);

    CompletableFuture<T> getFuture();

    default void queue() {
        queue(Context.DEFAULT_SUCCESS_HANDLER);
    }

    default void queue(final Consumer<? super T> success) {
        queue(success, Context.DEFAULT_ERROR_HANDLER);
    }

    void queue(final Consumer<? super T> success, final Consumer<? super Throwable> error);

    default void queueAfter(long time, TimeUnit unit) {
        queueAfter(Context.DEFAULT_SUCCESS_HANDLER, time, unit);
    }

    default void queueAfter(Consumer<? super T> success, long time, TimeUnit unit) {
        queueAfter(success, Context.DEFAULT_ERROR_HANDLER, time, unit);
    }

    void queueAfter(Consumer<? super T> success, final Consumer<? super Throwable> error, long time, TimeUnit unit);

    T complete();

}
