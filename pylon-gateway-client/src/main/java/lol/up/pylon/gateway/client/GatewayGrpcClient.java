package lol.up.pylon.gateway.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lol.up.pylon.gateway.client.entity.event.Event;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;
import lol.up.pylon.gateway.client.event.EventDispatcher;
import lol.up.pylon.gateway.client.event.EventSupplier;
import lol.up.pylon.gateway.client.service.GatewayCacheService;
import lol.up.pylon.gateway.client.util.ClosingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pylon.rpc.gateway.v1.cache.GatewayCacheGrpc;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GatewayGrpcClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(GatewayGrpcClient.class);

    private final ManagedChannel channel;
    private final GatewayCacheService cacheService;
    private final EventDispatcher eventDispatcher;

    private long defaultBotId;

    public GatewayGrpcClient(final long defaultBotId, final String host, final int port, final boolean enableRetry,
                             final ExecutorService eventExecutor) {
        this(defaultBotId, eventExecutor, enableRetry ?
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .enableRetry()
                        .build() :
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .build());
    }

    public GatewayGrpcClient(final long defaultBotId, final ExecutorService eventExecutor,
                             final ManagedChannel channel) {
        this.channel = channel;
        this.cacheService = new GatewayCacheService(this, GatewayCacheGrpc.newBlockingStub(channel));
        this.defaultBotId = defaultBotId;
        this.eventDispatcher = new EventDispatcher(eventExecutor);
    }

    public void setDefaultBotId(final long defaultBotId) {
        this.defaultBotId = defaultBotId;
    }

    public long getDefaultBotId() {
        return defaultBotId;
    }

    public GatewayCacheService getCacheService() {
        return cacheService;
    }

    public <E extends Event<E>> void registerReceiver(final Class<E> eventClass,
                                                      final AbstractEventReceiver<E> receiver) {
        eventDispatcher.registerReceiver(eventClass, receiver);
    }

    public Closeable registerEventSupplier(final EventSupplier eventSupplier) {
        final ClosingRunnable eventSupplierTask = eventSupplier.supplyEvents(eventDispatcher);
        Executors.newSingleThreadExecutor().submit(eventSupplierTask);
        return () -> {
            try {
                eventSupplierTask.stop();
            } catch (Exception exception) {
                log.error("Couldn't stop EventSupplier {}", eventSupplier.getClass().getCanonicalName(), exception);
            }
        };
    }

    @Override
    public void close() throws IOException {
        try {
            this.channel.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException("Couldn't await channel termination in time", e);
        }
    }
}
