package lol.up.pylon.gateway.client;

import bot.pylon.proto.gateway.v1.service.GatewayCacheGrpc;
import bot.pylon.proto.gateway.v1.service.GatewayRestGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.entity.event.Event;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;
import lol.up.pylon.gateway.client.event.EventDispatcher;
import lol.up.pylon.gateway.client.event.EventSupplier;
import lol.up.pylon.gateway.client.service.CacheService;
import lol.up.pylon.gateway.client.service.RestService;
import lol.up.pylon.gateway.client.util.ClosingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GatewayGrpcClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(GatewayGrpcClient.class);

    public static class GatewayGrpcClientBuilder {

        private long defaultBotId;
        private String routerHost;
        private int routerPort;
        private boolean enableRetry;
        private ExecutorService eventExecutor;

        private GatewayGrpcClientBuilder(final long defaultBotId) {
            this.defaultBotId = defaultBotId;
            this.eventExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        public GatewayGrpcClientBuilder setDefaultBotId(long defaultBotId) {
            this.defaultBotId = defaultBotId;
            return this;
        }

        public GatewayGrpcClientBuilder setRouterHost(String routerHost) {
            this.routerHost = routerHost;
            return this;
        }

        public GatewayGrpcClientBuilder setRouterPort(int routerPort) {
            this.routerPort = routerPort;
            return this;
        }

        public GatewayGrpcClientBuilder setEnableRetry(boolean enableRetry) {
            this.enableRetry = enableRetry;
            return this;
        }

        public GatewayGrpcClientBuilder setEventExecutor(ExecutorService eventExecutor) {
            this.eventExecutor = eventExecutor;
            return this;
        }

        public GatewayGrpcClient build() {
            if (defaultBotId == 0) {
                throw new NullPointerException("The default bot id must not be 0");
            }
            if (routerPort <= 0 || routerPort > 65535) {
                throw new IllegalArgumentException("The port must be greater than 0 and less than or equal to 65535");
            }
            Objects.requireNonNull(routerHost, "A routerHost is mandatory");
            return new GatewayGrpcClient(
                    defaultBotId,
                    routerHost,
                    routerPort,
                    enableRetry,
                    eventExecutor
            );
        }
    }

    public static GatewayGrpcClientBuilder builder(final long defaultBotId) {
        return new GatewayGrpcClientBuilder(defaultBotId);
    }

    private static GatewayGrpcClient instance;

    public static GatewayGrpcClient getSingleton() {
        return instance;
    }

    private final ManagedChannel channel;
    private final CacheService cacheService;
    private final RestService restService;
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

    private GatewayGrpcClient(final long defaultBotId, final ExecutorService eventExecutor,
                              final ManagedChannel channel) {
        if (instance != null) {
            throw new RuntimeException("There must be at most one instance of GatewayGrpcClient");
        }
        instance = this;
        this.channel = channel;
        this.cacheService = new CacheService(this, GatewayCacheGrpc.newBlockingStub(channel));
        this.restService = new RestService(this, GatewayRestGrpc.newBlockingStub(channel));
        this.defaultBotId = defaultBotId;
        this.eventDispatcher = new EventDispatcher(eventExecutor);
    }

    public void setDefaultBotId(final long defaultBotId) {
        this.defaultBotId = defaultBotId;
    }

    public long getDefaultBotId() {
        return defaultBotId;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public RestService getRestService() {
        return restService;
    }

    public User getSelfUser() {
        return getRestService().getSelfUser(0); // todo
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
