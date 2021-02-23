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

    private StreamObserver<WorkerStreamClientMessage> responseStream;
    private long seq;
    private CompletableFuture<Void> drainFuture;


    WorkerGroupStreamObserver(final WorkerGroupSupplier workerGroupSupplier,
                              final EventDispatcher eventDispatcher) {
        this.workerGroupSupplier = workerGroupSupplier;
        this.eventDispatcher = eventDispatcher;
        this.drained = new AtomicBoolean(false);
        this.seq = 0;
    }

    public void registerWorker(final StreamObserver<WorkerStreamClientMessage> responseStream) {
        this.responseStream = responseStream;
        responseStream.onNext(WorkerStreamClientMessage.newBuilder()
                .setIdentifyRequest(WorkerIdentifyRequest.newBuilder()
                        .setAuthToken(workerGroupSupplier.getAuthToken())
                        .setConsumerGroup(workerGroupSupplier.getConsumerGroup())
                        .setConsumerId(workerGroupSupplier.getConsumerId())
                        .setResumeSequence(seq)
                        .build())
                .build());
    }

    public CompletableFuture<Void> drainWorker() {
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
        } else if (message.hasHeartbeatResponse()) {
            handleHeartbeatResponse(message.getHeartbeatResponse());
        } else if (message.hasIdentifyResponse()) {
            handleIdentifyResponse(message.getIdentifyResponse());
        } else if (message.hasDrainResponse()) {
            handleDrainResponse(message.getDrainResponse());
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
    }

    private void handleEventEnvelope(final EventEnvelope eventEnvelope) {
        seq = eventEnvelope.getHeader().getSeq();
        eventDispatcher.dispatchEvent(eventEnvelope.getHeader(), eventEnvelope.getEvent());
        // todo: no response?
    }

    private void handleHeartbeatRequest(final WorkerHeartbeatRequest heartbeatRequest) {
        if (heartbeatRequest.getLastSequence() != seq) {
            log.warn("[Heartbeat] Got heartbeat request with seq {}, last received sequence was {}",
                    heartbeatRequest.getLastSequence(), seq);
        }
        responseStream.onNext(WorkerStreamClientMessage.newBuilder()
                .setHeartbeatResponse(WorkerHeartbeatResponse.newBuilder()
                        .setNonce(heartbeatRequest.getNonce())
                        .build())
                .build());
    }

    private void handleHeartbeatResponse(final WorkerHeartbeatResponse heartbeatResponse) {
        // todo: no response??
    }

    private void handleIdentifyResponse(final WorkerIdentifyResponse identifyResponse) {
        if (identifyResponse.getMissedEvents()) {
            log.warn("[Identify] This worker group has missed events!");
        }
        final WorkerIdentifyResponse.IdentifyStatus status = identifyResponse.getStatus();
        switch (status) {
            case OK:
                log.info("[Identify] Successfully subscribed to event stream for group {} with consumer {}",
                        workerGroupSupplier.getConsumerGroup(), workerGroupSupplier.getConsumerId());
                break;
            case ERROR:
                // todo: detailed error message
                log.error("[Identify] Unable to subscribe to event stream for group {} with consumer {}",
                        workerGroupSupplier.getConsumerGroup(), workerGroupSupplier.getConsumerId());
                break;
            case UNRECOGNIZED:
                log.error("[Identify] Unrecognized consumer group {}", workerGroupSupplier.getConsumerGroup());
                break;
            case UNKNOWN:
            default:
                log.error("[Identify] Unknown response received from server... Update client?");
                break;
        }
    }

    private void handleDrainResponse(final WorkerDrainResponse drainResponse) {
        log.info("Worker stopped received events. Completing...");
        drained.set(true);
    }

    private void handleUnknown() {
        log.error("Unknown server message received... Update client?");
    }
}
