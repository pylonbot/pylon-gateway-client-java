package lol.up.pylon.gateway.client.event;

import bot.pylon.proto.discord.v1.event.*;
import bot.pylon.proto.gateway.v1.service.GatewayDispatchGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lol.up.pylon.gateway.client.util.ClosingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

                        private void dispatch(final lol.up.pylon.gateway.client.entity.event.Event<? extends
                                lol.up.pylon.gateway.client.entity.event.Event<?>> event,
                                              final StreamObserver<EventResponse> response) {
                            eventDispatcher.dispatchEvent(event);
                            response.onNext(EventResponse.newBuilder().build());
                            response.onCompleted();
                        }

                        @Override
                        public void guildCreate(GuildCreateEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildUpdate(GuildUpdateEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildDelete(GuildDeleteEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void presenceUpdate(PresenceUpdateEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildMemberAdd(GuildMemberAddEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildMemberUpdate(GuildMemberUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildMemberRemove(GuildMemberRemoveEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void channelCreate(ChannelCreateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void channelUpdate(ChannelUpdateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void channelDelete(ChannelDeleteEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void channelPinsUpdate(ChannelPinsUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildRoleCreate(GuildRoleCreateEvent request,
                                                    StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildRoleUpdate(GuildRoleUpdateEvent request,
                                                    StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildRoleDelete(GuildRoleDeleteEvent request,
                                                    StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageCreate(MessageCreateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageUpdate(MessageUpdateEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageDelete(MessageDeleteEvent request,
                                                  StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageDeleteBulk(MessageDeleteBulkEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageReactionAdd(MessageReactionAddEvent request,
                                                       StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageReactionRemove(MessageReactionRemoveEvent request,
                                                          StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageReactionRemoveAll(MessageReactionRemoveAllEvent request,
                                                             StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void messageReactionRemoveEmoji(MessageReactionRemoveEmojiEvent request,
                                                               StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void typingStart(TypingStartEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void voiceStateUpdate(VoiceStateUpdateEvent request,
                                                     StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void voiceServerUpdate(VoiceServerUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void inviteCreate(InviteCreateEvent request,
                                                 StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void inviteDelete(InviteDeleteEvent request,
                                                 StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildBanAdd(GuildBanAddEvent request,
                                                StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildBanRemove(GuildBanRemoveEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildEmojisUpdate(GuildEmojisUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void guildIntegrationsUpdate(GuildIntegrationsUpdateEvent request,
                                                            StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void webhooksUpdate(WebhooksUpdateEvent request,
                                                   StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void integrationCreate(IntegrationCreateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void integrationUpdate(IntegrationUpdateEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void integrationDelete(IntegrationDeleteEvent request,
                                                      StreamObserver<EventResponse> responseObserver) {
                            dispatch(request, responseObserver);
                        }

                        @Override
                        public void interactionCreate(InteractionCreateEvent request,
                                                      StreamObserver<InteractionResponse> responseObserver) {
                            eventDispatcher.dispatchEvent(request);
                            responseObserver.onNext(InteractionResponse.newBuilder().build());
                            responseObserver.onCompleted();
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
        public void stop() throws Exception {
            if (server != null) {
                server.shutdown().shutdown();
            }
        }
    }
}
