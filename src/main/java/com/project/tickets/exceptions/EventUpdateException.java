package com.project.tickets.exceptions;

public class EventUpdateException extends EventTickectException{

    public EventUpdateException() {
    }

    public EventUpdateException(String message) {
        super(message);
    }

    public EventUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventUpdateException(Throwable cause) {
        super(cause);
    }

    public EventUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
