package lol.up.pylon.gateway.client.event;

import lol.up.pylon.gateway.client.util.ClosingRunnable;

@FunctionalInterface
public interface EventSupplier {

    ClosingRunnable supplyEvents(EventDispatcher dispatcher);

}
