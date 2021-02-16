package lol.up.pylon.gateway.client.service.request;

import javax.annotation.CheckReturnValue;
import java.util.function.BiFunction;
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
}
