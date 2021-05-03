package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class GrpcGatewayApiRateLimitedException extends GrpcGatewayApiException {

    public GrpcGatewayApiRateLimitedException(RestError apiError, String message, GrpcException source) {
        super(apiError, message, source);
    }
}
