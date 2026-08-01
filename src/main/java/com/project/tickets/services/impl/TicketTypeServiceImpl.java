package com.project.tickets.services.impl;

import com.project.tickets.domain.entities.Ticket;
import com.project.tickets.domain.entities.TicketStatusEnum;
import com.project.tickets.domain.entities.TicketType;
import com.project.tickets.domain.entities.User;
import com.project.tickets.exceptions.TicketTypeNotFoundException;
import com.project.tickets.exceptions.TicketsSoldOutException;
import com.project.tickets.exceptions.UserNotFoundException;
import com.project.tickets.repositories.TicketRepository;
import com.project.tickets.repositories.TicketTypeRepository;
import com.project.tickets.repositories.UserRepository;
import com.project.tickets.services.QrCodeService;
import com.project.tickets.services.TicketTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    //1.make obj of all necessary repo class to fecth data of different repository even we dont have its specifc
    //controller and services classes
    //2. make obj of service class which is necessary

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;


    //Transactional : multiple db entity calls : qr code , ticket type
    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with ID %s was not found", userId)
        ));

        TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(
                        String.format("Ticket type with ID %s was not found", ticketTypeId)
                ));

        int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
        Integer totalAvailable = ticketType.getTotalAvailable();

        if(purchasedTickets + 1 > totalAvailable) {
            throw new TicketsSoldOutException();
        }

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        ticket.setPurchaser(user);

        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);

        return ticketRepository.save(savedTicket);

    }
}

//purchaseTicket(userId, ticketTypeId)
//    │
//            ├── Find User
//    ├── Find Ticket Type (with DB lock)
//    ├── Check tickets remaining
//    ├── Create Ticket
//    ├── Save Ticket
//    ├── Generate QR Code
//    └── Save Ticket again
//    │
//            ▼
//Return Ticket
