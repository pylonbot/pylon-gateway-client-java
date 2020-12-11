package lol.up.pylon.gateway.client.event;

import bot.pylon.proto.discord.v1.event.EventEnvelope;
import bot.pylon.proto.discord.v1.event.EventEnvelopeAck;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStreamObserver implements StreamObserver<EventEnvelope> {

    private static final Logger log = LoggerFactory.getLogger(EventStreamObserver.class);

    private final ServerCallStreamObserver<EventEnvelopeAck> responseObserver;
    private final EventDispatcher eventDispatcher;

    public EventStreamObserver(final StreamObserver<EventEnvelopeAck> responseObserver, final EventDispatcher eventDispatcher) {
        this.responseObserver = (ServerCallStreamObserver<EventEnvelopeAck>) responseObserver;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void onNext(final EventEnvelope eventEnvelope) {
        responseObserver.onNext(EventEnvelopeAck.newBuilder()
                .setSeq(eventEnvelope.getHeader().getSeq())
                .build());
        try {
            eventDispatcher.dispatchEvent(eventEnvelope.getEvent());
        } catch (final Exception ex) {
            log.error("An unexpected error occurred when processing incoming event", ex);
        }
    }

    @Override
    public void onError(final Throwable throwable) {
        log.error("A downstream error occurred!", throwable);
        responseObserver.onCompleted();
    }

    @Override
    public void onCompleted() {
        log.info("onCompleted(): Event stream ended!");
        responseObserver.onCompleted();
    }
}
