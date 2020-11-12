package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class GrpcGatewayApiException extends RuntimeException {

    private final RestError apiError;

    public GrpcGatewayApiException(final RestError apiError, final String message) {
        super(message);
        this.apiError = apiError;
    }

    public RestError getApiError() {
        return apiError;
    }
}
