package lol.up.pylon.gateway.client.util;

public interface ClosingRunnable extends Runnable {

    void stop() throws Exception;
}
