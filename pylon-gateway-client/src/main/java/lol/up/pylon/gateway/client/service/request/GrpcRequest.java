package lol.up.pylon.gateway.client.service.request;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GrpcRequest<T> {

    @CheckReturnValue
    <V> GrpcRequest<V> transform(Function<T, V> transformer);

    @CheckReturnValue
    <V, P> GrpcRequest<V> transformWith(GrpcRequest<P> other, BiFunction<T, P, V> transformer);

    CompletableFuture<T> getFuture();

    void queue();

    void queue(final Consumer<? super T> success);

    void queue(final Consumer<? super T> success, final Consumer<Throwable> error);

    T complete();

}
