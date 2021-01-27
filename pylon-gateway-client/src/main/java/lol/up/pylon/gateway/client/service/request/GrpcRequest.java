package lol.up.pylon.gateway.client.service.request;

import javax.annotation.CheckReturnValue;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GrpcRequest<T> {

    @CheckReturnValue
    <V> GrpcRequest<V> transform(Function<T, V> transformer);

    void queue();

    void queue(final Consumer<? super T> success);

    void queue(final Consumer<? super T> success, final Consumer<Throwable> error);

    T complete();

}
