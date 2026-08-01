package com.project.tickets.domain.dtos;

import java.util.UUID;

import com.project.tickets.domain.entities.TicketValidationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationResponseDto {
    private UUID ticketId;
    private TicketValidationStatusEnum status;
}
