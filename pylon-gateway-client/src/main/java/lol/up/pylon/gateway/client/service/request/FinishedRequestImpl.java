package lol.up.pylon.gateway.client.service.request;

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
    public void queue() {

    }

    @Override
    public void queue(Consumer<? super T> success) {
        success.accept(entity);
    }

    @Override
    public void queue(Consumer<? super T> success, Consumer<Throwable> error) {
        success.accept(entity);
    }

    @Override
    public T complete() {
        return entity;
    }
}
