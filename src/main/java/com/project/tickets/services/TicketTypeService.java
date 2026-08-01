package com.project.tickets.services;


import com.project.tickets.domain.entities.Ticket;

import java.util.UUID;

public interface TicketTypeService {
    Ticket purchaseTicket(UUID userId, UUID ticketTypeId);
}
