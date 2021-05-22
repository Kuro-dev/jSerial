package org.kurodev.serializers.exception;

public class RecursiveDebthException extends RuntimeException {
    public RecursiveDebthException() {
    }

    public RecursiveDebthException(String message) {
        super(message);
    }

    public RecursiveDebthException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecursiveDebthException(Throwable cause) {
        super(cause);
    }

    public RecursiveDebthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
