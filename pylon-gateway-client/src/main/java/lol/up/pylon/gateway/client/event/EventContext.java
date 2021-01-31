package lol.up.pylon.gateway.client.event;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class EventContext {

    private static final ThreadLocal<EventContext> localContext = new ThreadLocal<>();

    public static ThreadLocal<EventContext> localContext() {
        return localContext;
    }

    public static EventContext current() {
        return localContext().get();
    }

    public static String buildContextKey(final String method, final long... params) {
        final StringBuilder sb = new StringBuilder(method);
        for (long param : params) {
            sb.append("-").append(param);
        }
        return sb.toString();
    }

    private static boolean cacheEnabled = true;

    public static void setContextRequestCacheEnabled(final boolean enabled) {
        cacheEnabled = enabled;
    }

    private final ExecutorService executorService;
    private final long botId;
    private final long guildId;
    private final Map<String, Object> contextCache;

    EventContext(final ExecutorService asyncExecutor, final long botId, final long guildId) {
        this.executorService = asyncExecutor;
        this.botId = botId;
        this.guildId = guildId;
        this.contextCache = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getContextObject(final String key) {
        if (cacheEnabled) {
            return (T) contextCache.get(key);
        }
        return null;
    }

    public void populateContext(final String key, final Object object) {
        if (cacheEnabled) {
            contextCache.put(key, object);
        }
    }

    public long getBotId() {
        return botId;
    }

    public long getGuildId() {
        return guildId;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void clearCache() {
        this.contextCache.clear();
    }

    public void clearCache(final String contextKey) {
        this.contextCache.remove(contextKey);
    }
}
