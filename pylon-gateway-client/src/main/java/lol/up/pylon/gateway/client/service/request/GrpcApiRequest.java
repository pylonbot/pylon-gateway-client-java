package lol.up.pylon.gateway.client.service.request;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GrpcApiRequest<T> extends GrpcRequest<T> {

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
