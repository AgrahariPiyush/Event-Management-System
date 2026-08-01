package com.project.tickets.services;

import com.project.tickets.domain.entities.TicketValidation;

import java.util.UUID;

public interface TicketValidationService {

    //validation : QR Code or Manually data check

    TicketValidation validateTicketByQrCode(UUID qrCodeId);
    TicketValidation validateTicketManually(UUID ticketId);
}
