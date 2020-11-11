package lol.up.pylon.gateway.client.exception;

import pylon.rpc.discord.v1.api.ApiError;

public class GrpcGatewayApiException extends RuntimeException {

    private final ApiError apiError;

    public GrpcGatewayApiException(final ApiError apiError, final String message) {
        super(message);
        this.apiError = apiError;
    }

    public ApiError getApiError() {
        return apiError;
    }
}
