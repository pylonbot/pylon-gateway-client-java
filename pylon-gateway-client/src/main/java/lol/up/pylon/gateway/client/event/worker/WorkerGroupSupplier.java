package lol.up.pylon.gateway.client.event.worker;

import bot.pylon.proto.gateway.v1.service.GatewayWorkerGroupGrpc;
import bot.pylon.proto.gateway.v1.workergroup.WorkerStreamClientMessage;
import io.grpc.CallCredentials;
import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.GatewayGrpcClient;
import lol.up.pylon.gateway.client.event.EventDispatcher;
import lol.up.pylon.gateway.client.util.ClosingRunnable;

import java.util.concurrent.Executor;

public class WorkerGroupSupplier implements ClosingRunnable {

    private final EventDispatcher eventDispatcher;
    private final GatewayWorkerGroupGrpc.GatewayWorkerGroupStub client;

    private final String authToken;
    private final String consumerGroup;
    private final String consumerId;


    private WorkerGroupStreamObserver streamObserver;
    private boolean reconnect = true;

    public WorkerGroupSupplier(final EventDispatcher eventDispatcher, final String authToken,
                               final String consumerGroup, final String consumerId) {
        this.eventDispatcher = eventDispatcher;
        this.authToken = authToken;
        this.consumerGroup = consumerGroup;
        this.consumerId = consumerId;

        this.client = GatewayWorkerGroupGrpc.newStub(GatewayGrpcClient.getSingleton().getGrpcChannel())
                .withCallCredentials(new CallCredentials() {
                    @Override
                    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor,
                                                     MetadataApplier applier) {

                    }

                    @Override
                    public void thisUsesUnstableApi() {

                    }
                });
    }

    private void registerWorkerGroupStream() {
        streamObserver = new WorkerGroupStreamObserver(this, eventDispatcher);
        final StreamObserver<WorkerStreamClientMessage> clientObserver = this.client.workerStream(streamObserver);
        streamObserver.registerWorker(clientObserver);
    }

    @Override
    public void stop() throws Exception {
        reconnect = false;
        streamObserver.drainWorker().get();

    }

    @Override
    public void run() {
        registerWorkerGroupStream();
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public String getConsumerId() {
        return consumerId;
    }
}
