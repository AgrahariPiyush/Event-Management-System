package com.project.tickets.domain.dtos;

import java.util.UUID;

import com.project.tickets.domain.entities.TicketValidationMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationRequestDto {
    private UUID id;
    private TicketValidationMethod method;
}
