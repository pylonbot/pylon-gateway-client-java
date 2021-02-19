package lol.up.pylon.gateway.client.service.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GrpcRequest<T> {

    class Context {

        private static final Logger log = LoggerFactory.getLogger(GrpcRequestImpl.class);

        private static final Consumer<Object> SUCCESS = o -> {};
        private static final Consumer<Throwable> ERROR = error -> log.error("An error occurred during grpc request",
                error);

        static Consumer<Object> DEFAULT_SUCCESS_HANDLER = SUCCESS;
        static Consumer<Throwable> DEFAULT_ERROR_HANDLER = ERROR;

        /**
         * Sets the default success handler for all {@link GrpcRequest GrpcRequests}
         *
         * @param success the success handler
         */
        public static void setDefaultSuccessHandler(final Consumer<Object> success) {
            if (success == null) {
                DEFAULT_SUCCESS_HANDLER = SUCCESS;
            } else {
                DEFAULT_SUCCESS_HANDLER = success;
            }
        }

        /**
         * Sets the default error handler for all {@link GrpcRequest GrpcRequests}
         *
         * @param error the error handler
         */
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

    /**
     * Subscribes to the request for the future and returns the future
     *
     * @return the Future for this request
     */
    @CheckReturnValue
    CompletableFuture<T> submit();

    /**
     * Subscribes & queues this request with the default success handler
     * Calls {@link GrpcRequest#queue(Consumer) GrpcRequest#queue(Consumer)}
     *
     * @see GrpcRequest#submit()
     * @see GrpcRequest#queue(Consumer)
     * @see GrpcRequest.Context#setDefaultSuccessHandler(Consumer)
     */
    default void queue() {
        queue(Context.DEFAULT_SUCCESS_HANDLER);
    }

    /**
     * Subscribes & queues this request with the specified success handler and the default error handler
     * Calls {@link GrpcRequest#queue(Consumer, Consumer) GrpcRequest#queue(Consumer, Consumer)}
     *
     * @param success the success handler for the result of the future
     * @see GrpcRequest#submit()
     * @see GrpcRequest#queue(Consumer, Consumer)
     * @see GrpcRequest.Context#setDefaultErrorHandler(Consumer)
     */
    default void queue(final Consumer<? super T> success) {
        queue(success, Context.DEFAULT_ERROR_HANDLER);
    }

    /**
     * Subscribes & queues this request with the specified success and error handler
     *
     * @param success the success handler for the result of the future
     * @param error the error handler for error events
     * @see GrpcRequest#submit()
     */
    void queue(final Consumer<? super T> success, final Consumer<? super Throwable> error);

    /**
     * Subscribes and blocks until the future returns
     *
     * @return the result of the future
     * @see GrpcRequest#submit()
     */
    T complete();

}
