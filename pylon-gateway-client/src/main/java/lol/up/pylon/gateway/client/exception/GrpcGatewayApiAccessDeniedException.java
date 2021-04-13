package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class GrpcGatewayApiAccessDeniedException extends GrpcGatewayApiException {

    public GrpcGatewayApiAccessDeniedException(RestError apiError, String message, GrpcException source) {
        super(apiError, message, source);
    }
}
