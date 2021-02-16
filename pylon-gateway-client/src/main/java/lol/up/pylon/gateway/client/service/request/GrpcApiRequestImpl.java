package lol.up.pylon.gateway.client.service.request;

import lol.up.pylon.gateway.client.exception.GrpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GrpcApiRequestImpl<T> extends GrpcRequestImpl<T> implements GrpcApiRequest<T> {


    public <V> GrpcApiRequestImpl(Executor executor, CompletableFuture<V> future, Function<V, T> transformer) {
        super(executor, future, transformer);
    }

    public GrpcApiRequestImpl(Executor executor, CompletableFuture<T> future) {
        super(executor, future);
    }

    public GrpcApiRequestImpl(Executor executor, CompletableFuture<T> future, GrpcException source) {
        super(executor, future, source);
    }

    @Override
    public GrpcApiRequest<T> reason(String reason) {
        return null;
    }

    @Override
    public GrpcApiRequest<T> retry(int limit) {
        return null;
    }

    @Override
    public <V> GrpcApiRequest<V> transform(Function<T, V> transformer) {
        return null;
    }

    @Override
    public <V> GrpcApiRequest<V> flatTransform(Function<T, GrpcRequest<V>> transformer) {
        return null;
    }

    @Override
    public <V, P> GrpcApiRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer) {
        return null;
    }
}
