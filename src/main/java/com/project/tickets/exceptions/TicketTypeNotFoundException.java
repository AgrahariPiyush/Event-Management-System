package com.project.tickets.exceptions;

public class TicketTypeNotFoundException extends EventTickectException{

    public TicketTypeNotFoundException() {
    }

    public TicketTypeNotFoundException(String message) {
        super(message);
    }

    public TicketTypeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TicketTypeNotFoundException(Throwable cause) {
        super(cause);
    }

    public TicketTypeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
