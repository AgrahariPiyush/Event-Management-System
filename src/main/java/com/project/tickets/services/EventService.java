package com.project.tickets.services;

import com.project.tickets.domain.CreateEventRequest;
import com.project.tickets.domain.UpdateEventRequest;
import com.project.tickets.domain.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.Optional;
import java.util.UUID;

public interface EventService {

    Event createEvent(UUID organizerId, CreateEventRequest event);
    Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable);
    Optional<Event> getEventForOrganizer(UUID organizerId, UUID eventId);
    Event updateEventForOrganizer(UUID organizerId, UUID eventId, UpdateEventRequest event);
    void deleteEventForOrganizer(UUID organizerId, UUID eventId);
    Page<Event> listPublishedEvents(Pageable pageable);
    //for searching event using query
    Page<Event> searchPublishedEvents(String query, Pageable pageable);
    Optional<Event> getPublishedEvent(UUID id);
}
