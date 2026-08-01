package com.project.tickets.controller;

import com.project.tickets.domain.CreateEventRequest;
import com.project.tickets.domain.UpdateEventRequest;
import com.project.tickets.domain.dtos.*;
import com.project.tickets.domain.entities.Event;
import com.project.tickets.mappers.EventMapper;
import com.project.tickets.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    //response dto are mapped using eventMapper here

    private final EventMapper eventMapper;
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<CreateEventResponseDto> createEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateEventRequestDto createEventRequestDto
    ){
        // Convert DTO to domain object
        CreateEventRequest createEventRequest = eventMapper.fromDto(createEventRequestDto);
        // Extract user ID from JWT
        UUID userId = UUID.fromString(jwt.getSubject());

        Event createdEvent = eventService.createEvent(userId, createEventRequest);
        // Convert response to DTO
        CreateEventResponseDto  createEventResponseDto = eventMapper.toDto(createdEvent);
        return new ResponseEntity<>(createEventResponseDto, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity<Page<ListEventResponseDto>> listEvents(
            @AuthenticationPrincipal Jwt jwt, Pageable pageable
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Page<Event> events = eventService.listEventsForOrganizer(userId, pageable);
        return ResponseEntity.ok(
                events.map(eventMapper::toListEventResponseDto)
        );
    }

    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GetEventDetailsResponseDto> getEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return eventService.getEventForOrganizer(userId, eventId)
                .map(eventMapper::toGetEventDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{eventId}")
    public ResponseEntity<UpdateEventResponseDto> updateEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequestDto updateEventRequestDto) {

        //map request entity to dto using mapper
        UpdateEventRequest updateEventRequest = eventMapper.fromDto(updateEventRequestDto);
         UUID userId = UUID.fromString(jwt.getSubject());

        Event updatedEvent = eventService.updateEventForOrganizer(
                userId, eventId, updateEventRequest
        );
        //fetch response dto using mapper
        UpdateEventResponseDto updateEventResponseDto = eventMapper.toUpdateEventResponseDto(
                updatedEvent);

        return ResponseEntity.ok(updateEventResponseDto);
    }


    @DeleteMapping(path = "/{eventId}")
    public ResponseEntity<Void> deleteEvent(
           @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        eventService.deleteEventForOrganizer(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}


