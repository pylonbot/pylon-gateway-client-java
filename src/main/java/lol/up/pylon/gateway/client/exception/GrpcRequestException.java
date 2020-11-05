package lol.up.pylon.gateway.client.exception;

public class GrpcRequestException extends RuntimeException {

    public GrpcRequestException(final String message) {
        super(message);
    }

    public GrpcRequestException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
