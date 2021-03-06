package lol.up.pylon.gateway.client;

import bot.pylon.proto.gateway.v1.service.GatewayCacheGrpc;
import bot.pylon.proto.gateway.v1.service.GatewayGrpc;
import bot.pylon.proto.gateway.v1.service.GatewayRestGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lol.up.pylon.gateway.client.entity.User;
import lol.up.pylon.gateway.client.entity.event.Event;
import lol.up.pylon.gateway.client.event.*;
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
import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
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
        private ExecutorConfig executorConfig;

        private GatewayGrpcClientBuilder(final long defaultBotId) {
            this.defaultBotId = defaultBotId;
            this.enableContextCache = true;
            this.executorConfig = new ExecutorConfig(this);
        }

        public ExecutorConfig executors() {
            return executorConfig;
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
         * See {@link DefaultEventDispatcher#dispatchEvent(bot.pylon.proto.discord.v1.event.EventEnvelope.HeaderData, Event)
         * EventDispatcher#dispatchEvent(Event)} for implementation details regarding the {@link EventContext
         * EventContext} lifetime.
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
                    executorConfig,
                    warnWithoutContext,
                    maxRestWaitDuration
            );
        }

        public class ExecutorConfig {

            private final GatewayGrpcClientBuilder builder;
            private ExecutorService eventExecutor;
            private ScheduledExecutorService asyncEventExecutor;
            private ExecutorService callbackExecutor;
            private ExecutorService cacheGrpcExecutor;
            private ScheduledExecutorService restGrpcExecutor;

            private ExecutorConfig(final GatewayGrpcClientBuilder builder) {
                this.builder = builder;
                this.eventExecutor = Executors.newFixedThreadPool(Math.max(8,
                        Runtime.getRuntime().availableProcessors() * 2));
                this.asyncEventExecutor = Executors.newScheduledThreadPool(Math.max(8,
                        Runtime.getRuntime().availableProcessors()) * 2);
                this.callbackExecutor = Executors.newWorkStealingPool(8);
                this.cacheGrpcExecutor = Executors.newFixedThreadPool(
                        Math.max(8, Runtime.getRuntime().availableProcessors()));
                this.restGrpcExecutor = Executors.newScheduledThreadPool(1024);
            }

            public GatewayGrpcClientBuilder client() {
                return builder;
            }

            public ExecutorService getEventExecutor() {
                return eventExecutor;
            }

            public ExecutorConfig setEventExecutor(ExecutorService eventExecutor) {
                this.eventExecutor = eventExecutor;
                return this;
            }

            public ScheduledExecutorService getAsyncEventExecutor() {
                return asyncEventExecutor;
            }

            public ExecutorConfig setAsyncEventExecutor(ScheduledExecutorService eventExecutor) {
                this.asyncEventExecutor = eventExecutor;
                return this;
            }

            public ExecutorService getCallbackExecutor() {
                return callbackExecutor;
            }

            public ExecutorConfig setCallbackExecutor(final ExecutorService callbackExecutor) {
                this.callbackExecutor = callbackExecutor;
                return this;
            }

            public ExecutorService getCacheGrpcExecutor() {
                return cacheGrpcExecutor;
            }

            public ExecutorConfig setCacheGrpcExecutor(final ExecutorService cacheGrpcExecutor) {
                this.cacheGrpcExecutor = cacheGrpcExecutor;
                return this;
            }

            public ScheduledExecutorService getRestGrpcExecutor() {
                return restGrpcExecutor;
            }

            public ExecutorConfig setRestGrpcExecutor(final ScheduledExecutorService restGrpcExecutor) {
                this.restGrpcExecutor = restGrpcExecutor;
                return this;
            }
        }
    }

    public static GatewayGrpcClientBuilder builder(final long defaultBotId) {
        return new GatewayGrpcClientBuilder(defaultBotId);
    }

    private static GatewayGrpcClient instance;

    public static GatewayGrpcClient getSingleton() {
        return instance;
    }

    private final GatewayGrpcClientBuilder.ExecutorConfig executorConfig;
    private final ManagedChannel channel;
    private final CacheService cacheService;
    private final RestService restService;
    private final GatewayService gatewayService;
    private EventDispatcher eventDispatcher;

    private long defaultBotId;
    private User selfUser;
    private long selfUserLastUpdate = 0;
    private final AtomicBoolean updating = new AtomicBoolean(false);

    public GatewayGrpcClient(final long defaultBotId, final String host, final int port, final boolean enableRetry,
                             final GatewayGrpcClientBuilder.ExecutorConfig executorConfig,
                             final boolean warnWithoutContext, final Duration maxRestWaitDuration) {
        this(defaultBotId, executorConfig, warnWithoutContext, maxRestWaitDuration, enableRetry ?
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .enableRetry()
                        .build() :
                ManagedChannelBuilder.forAddress(host, port)
                        .usePlaintext()
                        .executor(Executors.newWorkStealingPool(128))
                        .build());
    }

    private GatewayGrpcClient(final long defaultBotId, final GatewayGrpcClientBuilder.ExecutorConfig executorConfig,
                              final boolean warnWithoutContext, final Duration maxRestWaitDuration,
                              final ManagedChannel channel) {
        if (instance != null) {
            throw new RuntimeException("There must be at most one instance of GatewayGrpcClient");
        }
        instance = this;
        this.executorConfig = executorConfig;
        this.channel = channel;
        this.cacheService = new CacheService(this, GatewayCacheGrpc.newStub(channel),
                executorConfig.cacheGrpcExecutor, warnWithoutContext);
        this.restService = new RestService(this, GatewayRestGrpc.newStub(channel), executorConfig.restGrpcExecutor,
                warnWithoutContext, maxRestWaitDuration);
        this.gatewayService = new GatewayService(this, GatewayGrpc.newStub(channel), executorConfig.cacheGrpcExecutor,
                warnWithoutContext);
        this.defaultBotId = defaultBotId;
        this.eventDispatcher = new DefaultEventDispatcher(executorConfig.eventExecutor, executorConfig.asyncEventExecutor);
    }

    public void setEventDispatcher(@Nonnull final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Nonnull
    public EventDispatcher getEventDispatcher() {
        return eventDispatcher;
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

    public GatewayGrpcClientBuilder.ExecutorConfig getExecutorConfig() {
        return executorConfig;
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
            final ExecutorService cacheGrpcExecutor = getExecutorConfig().getCacheGrpcExecutor();
            return new GrpcRequestImpl<>(cacheGrpcExecutor, CompletableFuture.supplyAsync(() -> {
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
            }, cacheGrpcExecutor));
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

    public ManagedChannel getGrpcChannel() {
        return channel;
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
