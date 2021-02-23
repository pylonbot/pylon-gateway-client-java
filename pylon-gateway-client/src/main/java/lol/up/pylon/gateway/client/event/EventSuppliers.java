package lol.up.pylon.gateway.client.event;

import bot.pylon.proto.discord.v1.event.EventEnvelope;
import bot.pylon.proto.discord.v1.event.EventEnvelopeAck;
import bot.pylon.proto.gateway.v1.service.GatewayDispatchStreamingGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.util.ClosingRunnable;
import lol.up.pylon.gateway.client.event.worker.WorkerGroupSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

public class EventSuppliers {

    public static EventSupplier grpcServerEventSupplier(final int serverPort) {
        return dispatcher -> new GrpcEventSupplierServer(serverPort, dispatcher);
    }

    public static EventSupplier grpcWorkerGroupSupplier(final String authToken, final String consumerGroup,
                                                        final String consumerId) {
        return dispatcher -> new WorkerGroupSupplier(dispatcher, authToken, consumerGroup, consumerId);
    }

    private static class GrpcEventSupplierServer implements ClosingRunnable {

        private static final Logger log = LoggerFactory.getLogger(GrpcEventSupplierServer.class);

        private final Server server;

        private GrpcEventSupplierServer(final int port, final EventDispatcher eventDispatcher) {
            this.server = ServerBuilder.forPort(port)
                    .executor(Executors.newFixedThreadPool(64)) // todo flex
                    .addService(new GatewayDispatchStreamingGrpc.GatewayDispatchStreamingImplBase() {
                        @Override
                        public StreamObserver<EventEnvelope> event(StreamObserver<EventEnvelopeAck> responseObserver) {
                            return new EventStreamObserver(responseObserver, eventDispatcher);
                        }
                    })
                    .build();
        }

        @Override
        public void run() {
            try {
                this.server.start();
                log.info("gRPC Event Server started @ {}", server.getPort());
                this.server.awaitTermination();
            } catch (IOException e) {
                log.error("An error occurred when spinning up the gRPC event listener, events will not work", e);
            } catch (InterruptedException e) {
                log.error("The gRPC event listener stopped unexpectedly, events will no longer work", e);
            }
            log.info("gRPC event listener finished!");
        }

        @Override
        public void stop() {
            if (server != null) {
                server.shutdown().shutdown();
            }
        }
    }
}
