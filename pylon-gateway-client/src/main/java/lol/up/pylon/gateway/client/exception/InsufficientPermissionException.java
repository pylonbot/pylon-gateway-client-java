package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class InsufficientPermissionException extends GrpcGatewayApiAccessDeniedException {

    // todo

    public InsufficientPermissionException(RestError apiError, String message) {
        super(apiError, message);
    }
}
