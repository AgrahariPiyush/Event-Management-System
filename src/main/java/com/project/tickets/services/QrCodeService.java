package com.project.tickets.services;

import com.project.tickets.domain.entities.QrCode;
import com.project.tickets.domain.entities.Ticket;

import java.util.UUID;

public interface QrCodeService {

    QrCode generateQrCode(Ticket ticket);

    byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}
