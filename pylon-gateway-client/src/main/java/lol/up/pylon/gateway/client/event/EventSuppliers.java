package lol.up.pylon.gateway.client.event;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.util.ClosingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pylon.rpc.discord.v1.event.*;
import pylon.rpc.gateway.v1.dispatch.EventResponse;
import pylon.rpc.gateway.v1.dispatch.GatewayDispatchGrpc;
import pylon.rpc.gateway.v1.dispatch.InteractionEventResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EventSuppliers {

    public static EventSupplier grpcServerEventSupplier(final int serverPort) {
        return dispatcher -> new GrpcEventSupplierServer(serverPort, dispatcher);
    }

    public static EventSupplier grpcPollingEventSupplier(final String destinationHost, final int destinationPort) {
        throw new RuntimeException("This method is missing implementation");
    }

    private static class GrpcEventSupplierServer implements ClosingRunnable {

        private static final Logger log = LoggerFactory.getLogger(GrpcEventSupplierServer.class);

        private final Server server;

        private GrpcEventSupplierServer(final int port, final EventDispatcher eventDispatcher) {
            this.server = ServerBuilder.forPort(port)
                    .addService(new GatewayDispatchGrpc.GatewayDispatchImplBase() {
                        @Override
                        public void guildCreate(GuildCreateEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildUpdate(GuildUpdateEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildDelete(GuildDeleteEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void presenceUpdate(PresenceUpdateEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildMemberAdd(GuildMemberAddEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildMemberUpdate(GuildMemberUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildMemberRemove(GuildMemberRemoveEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void channelCreate(ChannelCreateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void channelUpdate(ChannelUpdateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void channelDelete(ChannelDeleteEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void channelPinsUpdate(ChannelPinsUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildRoleCreate(GuildRoleCreateEvent request,
                                                    StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildRoleUpdate(GuildRoleUpdateEvent request,
                                                    StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildRoleDelete(GuildRoleDeleteEvent request,
                                                    StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageCreate(MessageCreateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageUpdate(MessageUpdateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageDelete(MessageDeleteEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageDeleteBulk(MessageDeleteBulkEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageReactionAdd(MessageReactionAddEvent request,
                                                       StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageReactionRemove(MessageReactionRemoveEvent request,
                                                          StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageReactionRemoveAll(MessageReactionRemoveAllEvent request,
                                                             StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void messageReactionRemoveEmoji(MessageReactionRemoveEmojiEvent request,
                                                               StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void typingStart(TypingStartEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void voiceStateUpdate(VoiceStateUpdateEvent request,
                                                     StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void voiceServerUpdate(VoiceServerUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void inviteCreate(InviteCreateEvent request,
                                                 StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void inviteDelete(InviteDeleteEvent request,
                                                 StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildBanAdd(GuildBanAddEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildBanRemove(GuildBanRemoveEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildEmojisUpdate(GuildEmojisUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void guildIntegrationsUpdate(GuildIntegrationsUpdateEvent request,
                                                            StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void webhooksUpdate(WebhooksUpdateEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void integrationCreate(IntegrationCreateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void integrationUpdate(IntegrationUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void integrationDelete(IntegrationDeleteEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(EventResponse.newBuilder().build());
                        }

                        @Override
                        public void interactionCreate(InteractionCreateEvent request,
                                                      StreamObserver<InteractionEventResponse> responseObserver) {
                            log.warn("interactionCreate is not implemented {}", request);
                            responseObserver.onNext(InteractionEventResponse.newBuilder().build());
                        }
                    })
                    .build();
        }

        @Override
        public void run() {
            try {
                this.server.start();
                this.server.awaitTermination();
            } catch (IOException e) {
                log.error("An error occurred when spinning up the gRPC event listener, events will not work", e);
            } catch (InterruptedException e) {
                log.error("The gRPC event listener stopped unexpectedly, events will no longer work", e);
            }
        }

        @Override
        public void stop() throws Exception {
            if (server != null) {
                server.shutdown().shutdown();
            }
        }
    }
}
