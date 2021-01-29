package lol.up.pylon.gateway.client.event;

import java.util.concurrent.ExecutorService;

public class EventContext {

    private static final ThreadLocal<EventContext> localContext = new ThreadLocal<>();

    public static ThreadLocal<EventContext> localContext() {
        return localContext;
    }

    public static EventContext current() {
        return localContext().get();
    }

    private final ExecutorService executorService;
    private final long botId;
    private final long guildId;

    EventContext(final ExecutorService asyncExecutor, final long botId, final long guildId) {
        this.executorService = asyncExecutor;
        this.botId = botId;
        this.guildId = guildId;
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
}
