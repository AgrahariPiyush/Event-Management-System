package com.project.tickets.exceptions;

//dedicated exception types for ticket exceptions
public class EventTickectException extends RuntimeException {

    public EventTickectException() {
    }

    public EventTickectException(String message) {
        super(message);
    }

    public EventTickectException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTickectException(Throwable cause) {
        super(cause);
    }

    public EventTickectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
