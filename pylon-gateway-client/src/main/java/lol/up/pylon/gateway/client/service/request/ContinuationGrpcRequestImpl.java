package lol.up.pylon.gateway.client.service.request;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ContinuationGrpcRequestImpl<T> implements GrpcRequest<T> {

    private final AtomicBoolean shouldContinue;
    private final AtomicReference<T> oldValue;
    private final AtomicReference<GrpcRequest<T>> request;
    private final Function<T, GrpcRequest<T>> continuationSupplier;
    private final BiFunction<T, T, Boolean> continuePredicate;
    private final BiFunction<GrpcRequest<T>, T, GrpcRequest<T>> accumulator;

    public ContinuationGrpcRequestImpl(final GrpcRequest<T> init,
                                       final Function<T, GrpcRequest<T>> continuationSupplier,
                                       final BiFunction<T, T, Boolean> continuePredicate,
                                       final BiFunction<GrpcRequest<T>, T, GrpcRequest<T>> accumulator) {
        this.shouldContinue = new AtomicBoolean(true);
        this.oldValue = new AtomicReference<>();
        this.request = new AtomicReference<>(init);
        this.continuationSupplier = continuationSupplier;
        this.continuePredicate = continuePredicate;
        this.accumulator = accumulator;
    }

    private void buildContinuedRequest() {
        while (shouldContinue.get()) {
            request.getAndUpdate(request -> request.flatTransform(value -> {
                final T oldValue = this.oldValue.getAndSet(value);
                if (continuePredicate.apply(oldValue, value)) {
                    final GrpcRequest<T> continuationRequest = continuationSupplier.apply(value);
                    return accumulator.apply(continuationRequest, value);
                } else {
                    shouldContinue.set(false);
                }
                return new FinishedRequestImpl<>(value);
            }));
        }
    }

    private GrpcRequest<T> getRequest() {
        if (shouldContinue.get()) {
            buildContinuedRequest();
        }
        return request.get();
    }

    @Override
    public <V> GrpcRequest<V> transform(Function<T, V> transformer) {
        return getRequest().transform(transformer);
    }

    @Override
    public <V> GrpcRequest<V> flatTransform(Function<T, GrpcRequest<V>> transformer) {
        return getRequest().flatTransform(transformer);
    }

    @Override
    public <V, P> GrpcRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer) {
        return getRequest().transformWith(other, transformer);
    }

    @Override
    public CompletableFuture<T> getFuture() {
        return getRequest().getFuture();
    }

    @Override
    public void queue(Consumer<? super T> success, Consumer<? super Throwable> error) {
        getRequest().queue(success, error);
    }

    @Override
    public T complete() {
        return getRequest().complete();
    }
}
