package org.techpulse.producer.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
