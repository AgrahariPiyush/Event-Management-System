package com.project.tickets.mappers;

import com.project.tickets.domain.CreateEventRequest;
import com.project.tickets.domain.CreateTicketTypeRequest;
import com.project.tickets.domain.UpdateEventRequest;
import com.project.tickets.domain.UpdateTicketTypeRequest;
import com.project.tickets.domain.dtos.*;
import com.project.tickets.domain.entities.Event;
import com.project.tickets.domain.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

//Purpose : convert dtos and domain objects
//after making dto , make method here.
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDto dto);

    CreateEventRequest fromDto(CreateEventRequestDto dto);

    CreateEventResponseDto toDto(Event event);

    ListEventTicketTypeResponseDto toDto(TicketType ticketType);

    ListEventResponseDto toListEventResponseDto(Event event);

    GetEventDetailsTicketTypesResponseDto toGetEventDetailsTicketTypesResponseDto(
            TicketType ticketType);

    GetEventDetailsResponseDto toGetEventDetailsResponseDto(Event event);

    UpdateTicketTypeRequest fromDto(UpdateTicketTypeRequestDto dto);

    UpdateEventRequest fromDto(UpdateEventRequestDto dto);

    UpdateTicketTypeResponseDto toUpdateTicketTypeResponseDto(TicketType ticketType);

    UpdateEventResponseDto toUpdateEventResponseDto(Event event);

    ListEventResponseDto toListPublishedEventResponseDto(Event event);

    //for published event response dto (ticketype + published event)
    GetPublishedEventDetailsTicketTypesResponseDto toGetPublishedEventDetailsTicketTypesResponseDto(TicketType ticketType);
    GetPublishedEventDetailsResponseDto toGetPublishedEventDetailsResponseDto(Event event);
}
