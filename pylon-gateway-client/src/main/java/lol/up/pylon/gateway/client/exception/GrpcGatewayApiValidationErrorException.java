package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class GrpcGatewayApiValidationErrorException extends GrpcGatewayApiException {

    public GrpcGatewayApiValidationErrorException(RestError apiError, String message, GrpcException source) {
        super(apiError, message, source);
    }
}
