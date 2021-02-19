package lol.up.pylon.gateway.client.service.request;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GrpcApiRequest<T> extends GrpcRequest<T> {

    /**
     * Returns true if this representation of a {@link java.util.concurrent.CompletableFuture CompletableFuture}
     * can be altered with {@link GrpcApiRequest#reason(String) GrpcApiRequest#reason(String)} or
     * {@link GrpcApiRequest#retry(int) GrpcApiRequest#retry(int)}.
     * <p>
     * If it returns false, this request has already been transformed and is no longer mutable.
     *
     * @return true if mutable
     */
    boolean isMutable();

    /**
     * Sets the AuditLogReason for this request, if applicable
     *
     * @param reason the reason to show in audit-log
     * @return this request
     */
    @CheckReturnValue
    GrpcApiRequest<T> reason(String reason);

    @CheckReturnValue
    GrpcApiRequest<T> retry(int limit);

    @Override
    @CheckReturnValue
    <V> GrpcApiRequest<V> transform(Function<T, V> transformer);

    @Override
    @CheckReturnValue
    <V> GrpcApiRequest<V> flatTransform(Function<T, GrpcRequest<V>> transformer);

    @Override
    @CheckReturnValue
    <V, P> GrpcApiRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer);

    default void queueAfter(long time, TimeUnit unit) {
        queueAfter(Context.DEFAULT_SUCCESS_HANDLER, time, unit);
    }

    default void queueAfter(Consumer<? super T> success, long time, TimeUnit unit) {
        queueAfter(success, Context.DEFAULT_ERROR_HANDLER, time, unit);
    }

    void queueAfter(Consumer<? super T> success, final Consumer<? super Throwable> error, long time, TimeUnit unit);
}
