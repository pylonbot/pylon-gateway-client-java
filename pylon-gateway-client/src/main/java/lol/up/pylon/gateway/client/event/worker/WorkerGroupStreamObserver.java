package lol.up.pylon.gateway.client.event.worker;

import bot.pylon.proto.discord.v1.event.EventEnvelope;
import bot.pylon.proto.gateway.v1.workergroup.*;
import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.event.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerGroupStreamObserver implements StreamObserver<WorkerStreamServerMessage> {

    private static final Logger log = LoggerFactory.getLogger(WorkerGroupStreamObserver.class);

    private final WorkerGroupSupplier workerGroupSupplier;
    private final EventDispatcher eventDispatcher;
    private final AtomicBoolean drained;
    private final AtomicBoolean draining;

    private StreamObserver<WorkerStreamClientMessage> responseStream;
    private long seq;
    private CompletableFuture<Void> drainFuture;


    WorkerGroupStreamObserver(final WorkerGroupSupplier workerGroupSupplier,
                              final EventDispatcher eventDispatcher) {
        this.workerGroupSupplier = workerGroupSupplier;
        this.eventDispatcher = eventDispatcher;
        this.drained = new AtomicBoolean(false);
        this.draining = new AtomicBoolean(false);
        this.seq = 0;
    }

    public void registerWorker(final StreamObserver<WorkerStreamClientMessage> responseStream) {
        this.responseStream = responseStream;
        responseStream.onNext(WorkerStreamClientMessage.newBuilder()
                .setIdentifyRequest(WorkerIdentifyRequest.newBuilder()
                        .setAuthToken(workerGroupSupplier.getAuthToken())
                        .setConsumerGroup(workerGroupSupplier.getConsumerGroup())
                        .setConsumerId(workerGroupSupplier.getConsumerId())
                        .setLastSequence(seq)
                        .build())
                .build());
    }

    public CompletableFuture<Void> drainWorker() {
        if (draining.get()) {
            return drainFuture == null ? CompletableFuture.completedFuture((Void) null) : drainFuture;
        }
        draining.set(true);
        responseStream.onNext(WorkerStreamClientMessage.newBuilder()
                .setDrainRequest(WorkerDrainRequest.newBuilder().build())
                .build());
        drainFuture = new CompletableFuture<>();
        return drainFuture;
    }

    @Override
    public void onNext(WorkerStreamServerMessage message) {
        if (message.hasEventEnvelope()) {
            handleEventEnvelope(message.getEventEnvelope());
        } else if (message.hasHeartbeatRequest()) {
            handleHeartbeatRequest(message.getHeartbeatRequest());
        } else if (message.hasIdentifyResponse()) {
            handleIdentifyResponse(message.getIdentifyResponse());
        } else if (message.hasStreamClosed()) {
            handleStreamClosed(message.getStreamClosed());
        } else {
            handleUnknown();
        }
    }

    @Override
    public void onError(final Throwable error) {
        log.error("An error was observed in the grpc stream", error);
    }

    @Override
    public void onCompleted() {
        if (drained.get()) {
            responseStream.onCompleted();
            drainFuture.complete(null);
            drainFuture = null;
            return;
        }
        log.warn("Connection completed but the worker wasn't drained!");
        if (!draining.get()) {
            workerGroupSupplier.connectWorker(); // reconnect if the worker isn't draining
        }
    }

    private void handleEventEnvelope(final EventEnvelope eventEnvelope) {
        if (draining.get() || drained.get()) {
            log.debug("Dropped event during/after draining");
            return;
        }
        seq = eventEnvelope.getHeader().getSeq();
        eventDispatcher.dispatchEvent(eventEnvelope.getHeader(), eventEnvelope.getEvent());
    }

    private void handleHeartbeatRequest(final WorkerHeartbeatRequest heartbeatRequest) {
        log.debug("[Heartbeat] Received heartbeat request from gateway. Nonce: {}",
                heartbeatRequest.getNonce());
        responseStream.onNext(WorkerStreamClientMessage.newBuilder()
                .setHeartbeatAck(WorkerHeartbeatAck.newBuilder()
                        .setSequence(seq)
                        .setNonce(heartbeatRequest.getNonce())
                        .build())
                .build());
    }

    private void handleIdentifyResponse(final WorkerIdentifyResponse identifyResponse) {
        log.info("Received WorkerIdentifyResponse with router ticket {}", identifyResponse.getRouterTicket());
    }

    private void handleStreamClosed(final WorkerStreamClosed workerStreamClosed) {
        log.info("Worker stopped received events with reason {}. Completing...", workerStreamClosed.getReason().name());
        drained.set(true);
    }

    private void handleUnknown() {
        log.error("Unknown server message received... Update client?");
    }
}
