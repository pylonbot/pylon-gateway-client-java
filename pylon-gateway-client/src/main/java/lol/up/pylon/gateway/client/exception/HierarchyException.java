package lol.up.pylon.gateway.client.exception;

import bot.pylon.proto.discord.v1.rest.RestError;

public class HierarchyException extends GrpcGatewayApiAccessDeniedException {

    // todo

    public HierarchyException(RestError apiError, String message) {
        super(apiError, message);
    }
}
