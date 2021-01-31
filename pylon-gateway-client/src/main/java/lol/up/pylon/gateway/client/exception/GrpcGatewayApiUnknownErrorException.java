package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class GrpcGatewayApiUnknownErrorException extends GrpcGatewayApiException {

    public GrpcGatewayApiUnknownErrorException(RestError apiError, String message) {
        super(apiError, message);
    }
}
