package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class GrpcGatewayApiResourceNotFoundException extends GrpcGatewayApiException {

    public GrpcGatewayApiResourceNotFoundException(RestError apiError, String message) {
        super(apiError, message);
    }
}
