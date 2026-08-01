package com.project.tickets.exceptions;

public class EventNotFoundException extends EventTickectException{

    public EventNotFoundException() {
    }

    public EventNotFoundException(String message) {
        super(message);
    }

    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventNotFoundException(Throwable cause) {
        super(cause);
    }

    public EventNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
