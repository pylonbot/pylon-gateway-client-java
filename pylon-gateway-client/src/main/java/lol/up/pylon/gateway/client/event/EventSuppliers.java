package lol.up.pylon.gateway.client.event;

public class EventSuppliers {

    public static EventSupplier grpcListeningEventSupplier(final String serverHost, final int serverPort) {
        return dispatcher -> () -> {
            // todo: wait for event.proto service definition
        };
    }

    public static EventSupplier grpcPollingEventSupplier(final String destinationHost, final int destinationPort) {
        throw new RuntimeException("This method is missing implementation");
    }
}
