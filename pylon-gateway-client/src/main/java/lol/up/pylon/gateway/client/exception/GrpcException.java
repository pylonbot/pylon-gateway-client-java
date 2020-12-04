package lol.up.pylon.gateway.client.exception;

public class GrpcException extends RuntimeException {

    public GrpcException(final String message) {
        super(message);
    }

    public GrpcException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
