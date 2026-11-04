package org.techpulse.consumer.exception;

public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException(String message) {
        super(message);
    }

    public DuplicateEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
