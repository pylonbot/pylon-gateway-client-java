package lol.up.pylon.gateway.client;

import bot.pylon.proto.gateway.v1.service.GatewayCacheGrpc;
import bot.pylon.proto.gateway.v1.service.GatewayGrpc;
import bot.pylon.proto.gateway.v1.service.GatewayRestGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.entity.event.Event;
import lol.up.pylon.gateway.client.event.AbstractEventReceiver;
import lol.up.pylon.gateway.client.event.EventContext;
import lol.up.pylon.gateway.client.event.EventDispatcher;
import lol.up.pylon.gateway.client.event.EventSupplier;
import lol.up.pylon.gateway.client.service.CacheService;
import lol.up.pylon.gateway.client.service.GatewayService;
import lol.up.pylon.gateway.client.service.RestService;
import lol.up.pylon.gateway.client.service.request.FinishedRequestImpl;
import lol.up.pylon.gateway.client.service.request.GrpcRequest;
import lol.up.pylon.gateway.client.service.request.GrpcRequestImpl;
import lol.up.pylon.gateway.client.util.ClosingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GatewayGrpcClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(GatewayGrpcClient.class);

    public static class GatewayGrpcClientBuilder {

        private long defaultBotId;
        private String routerHost;
        private int routerPort;
        private boolean enableRetry;
        private boolean enableContextCache;
        private boolean warnWithoutContext = false;
        private Duration maxRestWaitDuration = Duration.ofSeconds(10);
        private ExecutorService eventExecutor;
        private ExecutorService grpcExecutor;

        private GatewayGrpcClientBuilder(final long defaultBotId) {
            this.defaultBotId = defaultBotId;
            this.enableContextCache = true;
            this.eventExecutor = Executors.newFixedThreadPool(Math.max(8,
                    Runtime.getRuntime().availableProcessors() * 2));
            this.grpcExecutor = Executors.newFixedThreadPool(
                    Math.max(8, Runtime.getRuntime().availableProcessors()));
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

        /**
         * Cache repeated calls on the same cache entity during one {@link EventContext EventContext} if set to true
         * One {@link EventContext EventContext} is valid for all listeners and is cleared as the listeners completed.
         * See {@link EventDispatcher#dispatchEvent(Event) EventDispatcher#dispatchEvent(Event)} for implementation
         * details regarding the {@link EventContext EventContext} lifetime.
         * <p>
         * To manually clear an {@link EventContext EventContext} you can use {@link EventContext#clearCache()}.
         * To manually clear one specific entity from the {@link EventContext EventContext} cache, you can use
         * {@link EventContext#clearCache(String) EventContext#clearCache(String)} with the String being a generated
         * context key from {@link EventContext#buildContextKey(String, long...) EventContext#buildContextKey(String,
         * long...)} with the longs being all related snowflakes (botId, guildId, and so on), see {@link CacheService
         * CacheService} implementation
         *
         * @param enableContextCache whether the described context-cache should be enabled
         * @return builder object for the {@link GatewayGrpcClient GatewayGrpcClient}
         */
        public GatewayGrpcClientBuilder setContextCacheEnabled(boolean enableContextCache) {
            this.enableContextCache = enableContextCache;
            return this;
        }

        public GatewayGrpcClientBuilder setWarnWithoutContext(final boolean warnWithoutContext) {
            this.warnWithoutContext = warnWithoutContext;
            return this;
        }

        public GatewayGrpcClientBuilder setEventExecutor(ExecutorService eventExecutor) {
            this.eventExecutor = eventExecutor;
            return this;
        }

        public GatewayGrpcClientBuilder setGrpcExecutor(final ExecutorService grpcExecutor) {
            this.grpcExecutor = grpcExecutor;
            return this;
        }

        public void setMaxRestWaitDuration(Duration maxRestWaitDuration) {
            this.maxRestWaitDuration = maxRestWaitDuration;
        }

        public GatewayGrpcClient build() {
            if (defaultBotId == 0) {
                throw new NullPointerException("The default bot id must not be 0");
            }
            if (routerPort <= 0 || routerPort > 65535) {
                throw new IllegalArgumentException("The port must be greater than 0 and less than or equal to 65535");
            }
            Objects.requireNonNull(routerHost, "A routerHost is mandatory");
            EventContext.setContextRequestCacheEnabled(enableContextCache);
            return new GatewayGrpcClient(
                    defaultBotId,
                    routerHost,
                    routerPort,
                    enableRetry,
                    eventExecutor,
                    grpcExecutor,
                    warnWithoutContext,
                    maxRestWaitDuration
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

    private final ExecutorService grpcExecutor;
    private final ManagedChannel channel;
    private final CacheService cacheService;
    private final RestService restService;
    private final GatewayService gatewayService;
    private final EventDispatcher eventDispatcher;

    private long defaultBotId;
    private User selfUser;
    private long selfUserLastUpdate = 0;
    private final AtomicBoolean updating = new AtomicBoolean(false);

    public GatewayGrpcClient(final long defaultBotId, final String host, final int port, final boolean enableRetry,
                             final ExecutorService event, final ExecutorService grpc,
                             final boolean warnWithoutContext, final Duration maxRestWaitDuration) {
        this(defaultBotId, event, grpc, warnWithoutContext, maxRestWaitDuration, enableRetry ?
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .enableRetry()
                        .build() :
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .executor(Executors.newWorkStealingPool(128))
                        .build());
    }

    private GatewayGrpcClient(final long defaultBotId, final ExecutorService event, final ExecutorService grpc,
                              final boolean warnWithoutContext, final Duration maxRestWaitDuration,
                              final ManagedChannel channel) {
        if (instance != null) {
            throw new RuntimeException("There must be at most one instance of GatewayGrpcClient");
        }
        instance = this;
        this.grpcExecutor = grpc;
        this.channel = channel;
        this.cacheService = new CacheService(this, GatewayCacheGrpc.newStub(channel), grpc, warnWithoutContext);
        this.restService = new RestService(this, GatewayRestGrpc.newStub(channel), grpc, warnWithoutContext,
                maxRestWaitDuration);
        this.gatewayService = new GatewayService(this, GatewayGrpc.newStub(channel), grpc, warnWithoutContext);
        this.defaultBotId = defaultBotId;
        this.eventDispatcher = new EventDispatcher(event);
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

    public GatewayService getGatewayService() {
        return gatewayService;
    }

    public ExecutorService getGrpcExecutor() {
        return grpcExecutor;
    }

    @CheckReturnValue
    public GrpcRequest<User> getSelfUser() {
        final EventContext context = EventContext.current();
        final long botId;
        if (context != null) {
            botId = context.getBotId();
        } else {
            botId = getDefaultBotId();
        }
        if (selfUser != null && (System.currentTimeMillis() - selfUserLastUpdate < 60_000 || updating.get())) {
            return new FinishedRequestImpl<>(selfUser);
        } else {
            updating.set(true);
            return new GrpcRequestImpl<>(getGrpcExecutor(), CompletableFuture.supplyAsync(() -> {
                try {
                    this.selfUser = getGatewayService().findUser(botId)
                            .flatTransform(user -> {
                                if (user == null) {
                                    return getRestService().getSelfUser(0);
                                } else {
                                    return new FinishedRequestImpl<>(user);
                                }
                            }).complete();
                    this.selfUserLastUpdate = System.currentTimeMillis();
                } catch (final Throwable ex) {
                    log.error("An error occurred when trying to get self-user", ex);
                } finally {
                    updating.set(false);
                }
                return this.selfUser;
            }, getGrpcExecutor()));
        }
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
