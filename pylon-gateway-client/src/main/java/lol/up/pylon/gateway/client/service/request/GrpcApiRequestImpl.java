package lol.up.pylon.gateway.client.service.request;

import com.google.protobuf.StringValue;
import lol.up.pylon.gateway.client.exception.GrpcException;
import lol.up.pylon.gateway.client.exception.GrpcRequestException;
import lol.up.pylon.gateway.client.util.CompletableFutureStreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class GrpcApiRequestImpl<T> extends GrpcRequestImpl<T> implements GrpcApiRequest<T> {

    private static final Logger log = LoggerFactory.getLogger(GrpcApiRequest.class);

    private final Object requestData;
    private boolean mutable;

    public <V> GrpcApiRequestImpl(final ScheduledExecutorService executor, final Object requestData,
                                  final CompletableFutureStreamObserver<V> future,
                                  final Function<V, T> transformer, final Runnable request) {
        super(executor, future, transformer, request);
        this.requestData = requestData;
        this.mutable = true;
    }

    public GrpcApiRequestImpl(final ScheduledExecutorService executor, final CompletableFuture<T> future,
                              final Runnable request,
                              final GrpcException source) {
        super(executor, future, request, source);
        this.requestData = null;
        this.mutable = false;
    }

    private ScheduledExecutorService executor() {
        return (ScheduledExecutorService) executor;
    }

    private CompletableFutureStreamObserver<T> future() {
        return (CompletableFutureStreamObserver<T>) future;
    }


    @Override
    public final boolean isMutable() {
        return mutable;
    }

    @Override
    public CompletableFuture<T> submit() {
        this.mutable = false;
        return super.submit();
    }

    @Override
    public GrpcApiRequest<T> reason(final String reason) {
        if (!isMutable()) {
            throw new GrpcRequestException("This GrpcApiRequest is immutable");
        }
        if(requestData == null) {
            return this;
        }
        try {
            final Field field = requestData.getClass().getDeclaredField("auditLogReason_");
            field.setAccessible(true);
            field.set(requestData, StringValue.of(reason));
        } catch (final Exception ex) {
            log.error("Couldn't set audit log reason for request {}", requestData.getClass().getName(), ex);
        }
        return this;
    }

    @Override
    public GrpcApiRequest<T> retry(final int limit) {
        if (!isMutable()) {
            throw new GrpcRequestException("This GrpcApiRequest is immutable");
        }
        future().setRetryLimit(limit);
        return this;
    }

    @Override
    public <V> GrpcApiRequest<V> transform(final Function<T, V> transformer) {
        return new GrpcApiRequestImpl<V>(executor(), getFuture().thenApply(transformer), request, source);
    }

    @Override
    public <V> GrpcApiRequest<V> flatTransform(final Function<T, GrpcRequest<V>> transformer) {
        final CompletableFuture<V> transformed = getFuture()
                .thenApplyAsync(transformer, executor)
                .thenComposeAsync(GrpcRequest::getFuture, executor);
        return new GrpcApiRequestImpl<V>(executor(), transformed, request, source);
    }

    @Override
    public <V, P> GrpcApiRequest<V> transformWith(final GrpcRequest<P> other, BiFunction<T, P, V> transformer) {
        final CompletableFuture<V> transformed = getFuture()
                .thenCombineAsync(other.getFuture(), transformer, executor);
        return new GrpcApiRequestImpl<V>(executor(), transformed, request, source);
    }

    @Override
    public void queueAfter(final Consumer<? super T> success, final Consumer<? super Throwable> error, final long time,
                           final TimeUnit unit) {
        executor().schedule(() -> queue(success, error), time, unit);
    }
}
