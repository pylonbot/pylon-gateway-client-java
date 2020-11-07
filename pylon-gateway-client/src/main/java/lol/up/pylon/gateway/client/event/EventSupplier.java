package lol.up.pylon.gateway.client.event;

@FunctionalInterface
public interface EventSupplier {

    Runnable supplyEvents(EventDispatcher dispatcher);

}
