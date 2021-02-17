package lol.up.pylon.gateway.client.service.request;

import lol.up.pylon.gateway.client.GatewayGrpcClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class FinishedRequestImpl<T> implements GrpcRequest<T> {

    private final T entity;

    public FinishedRequestImpl(final T entity) {
        this.entity = entity;
    }

    @Override
    public <V> GrpcRequest<V> transform(Function<T, V> transformer) {
        return new FinishedRequestImpl<>(transformer.apply(entity));
    }

    @Override
    public <V> GrpcRequest<V> flatTransform(Function<T, GrpcRequest<V>> transformer) {
        return transformer.apply(entity);
    }

    @Override
    public <V, P> GrpcRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer) {
        final ExecutorService executor = GatewayGrpcClient.getSingleton().getExecutorConfig().getCallbackExecutor();
        final CompletableFuture<V> future = getFuture().thenCombineAsync(other.getFuture(), transformer, executor);
        return new GrpcRequestImpl<>(executor, future);
    }

    @Override
    public CompletableFuture<T> getFuture() {
        return CompletableFuture.completedFuture(entity);
    }

    @Override
    public void queue(Consumer<? super T> success, Consumer<? super Throwable> error) {
        success.accept(entity);
    }

    @Override
    public T complete() {
        return entity;
    }
}
