package lol.up.pylon.gateway.client.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(final Throwable throwable) {
        super(throwable);
    }

    public ValidationException(final String message) {
        super(message);
    }

    public ValidationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
