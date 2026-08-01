package com.project.tickets.services.impl;

import com.project.tickets.domain.CreateEventRequest;
import com.project.tickets.domain.UpdateEventRequest;
import com.project.tickets.domain.UpdateTicketTypeRequest;
import com.project.tickets.domain.entities.Event;
import com.project.tickets.domain.entities.EventStatusEnum;
import com.project.tickets.domain.entities.TicketType;
import com.project.tickets.domain.entities.User;
import com.project.tickets.exceptions.EventNotFoundException;
import com.project.tickets.exceptions.EventUpdateException;
import com.project.tickets.exceptions.TicketTypeNotFoundException;
import com.project.tickets.exceptions.UserNotFoundException;
import com.project.tickets.repositories.EventRepository;
import com.project.tickets.repositories.UserRepository;
import com.project.tickets.services.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {


    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    //---------------------------------------------------------------------------------------------
    //Event API methods

    @Override
    @Transactional
    public Event createEvent(UUID organizerId, CreateEventRequest event) {
        User organizer =  userRepository.findById(organizerId).orElseThrow(()->
                new UserNotFoundException(String.format("User with id %s not found", organizerId)));

        Event eventToCreate = new  Event();

        List<TicketType> ticketTypesToCreate = event.getTicketTypes().stream().map(
                ticketType -> {
                    TicketType ticketTypeToCreate = new TicketType();
                    ticketTypeToCreate.setName(ticketType.getName());
                    ticketTypeToCreate.setPrice(ticketType.getPrice());
                    ticketTypeToCreate.setDescription(ticketType.getDescription());
                    ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
                    ticketTypeToCreate.setEvent(eventToCreate);
                    return ticketTypeToCreate;
                }).toList();


        eventToCreate.setName(event.getName());
        eventToCreate.setStart(event.getStart());
        eventToCreate.setEnd(event.getEnd());
        eventToCreate.setVenue(event.getVenue());
        eventToCreate.setSalesStart(event.getSalesStart());
        eventToCreate.setSalesEnd(event.getSalesEnd());
        eventToCreate.setStatus(event.getStatus());
        eventToCreate.setOrganizer(organizer);
        eventToCreate.setTicketTypes(ticketTypesToCreate);

        return eventRepository.save(eventToCreate);

    }

    //list events for organizer
    @Override
    public Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable) {
        return eventRepository.findByOrganizerId(organizerId, pageable);
    }

    @Override
    public Optional<Event> getEventForOrganizer(UUID organizerId, UUID id) {
        return eventRepository.findByIdAndOrganizerId(id,organizerId);
    }

    //---------------------------------------------------------------------------------------------
    //Update API method

    @Override
    @Transactional
    public Event updateEventForOrganizer(UUID organizerId, UUID id, UpdateEventRequest event) {

        //Transactional is addded since we are doing multiple db calls so we need they are all in
        //consistent state

        //event id does not exist
        if (null == event.getId()) {
            throw new EventUpdateException("Event ID cannot be null");
        }

        //event doest not matches
        if (!id.equals(event.getId())) {
            throw new EventUpdateException("Cannot update the ID of an event");
        }

        //fetch event using event Repo , if not found throw exception
        Event existingEvent = eventRepository
                .findByIdAndOrganizerId(id, organizerId)
                .orElseThrow(() -> new EventNotFoundException(
                        String.format("Event with ID '%s' does not exist", id))
                );

        //update feilds from entity which we received from client
        existingEvent.setName(event.getName());
        existingEvent.setStart(event.getStart());
        existingEvent.setEnd(event.getEnd());
        existingEvent.setVenue(event.getVenue());
        existingEvent.setSalesStart(event.getSalesStart());
        existingEvent.setSalesEnd(event.getSalesEnd());
        existingEvent.setStatus(event.getStatus());

        //ftech requestTicketType ids : nonNull ones
        Set<UUID> requestTicketTypeIds = event.getTicketTypes()
                .stream()
                .map(UpdateTicketTypeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //rmeove those tickettype ids from current which are not in curent updateEventRequest event
        existingEvent.getTicketTypes().removeIf(existingTicketType ->
                !requestTicketTypeIds.contains(existingTicketType.getId())
        );

        //collect id and tickettype in map from current request using streams
        Map<UUID, TicketType> existingTicketTypesIndex = existingEvent.getTicketTypes().stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        //update all tickettype info present in request updateEvent
        //cases : is Null (creste from scratch) , exists (fetch from map) , does not exists(exception)
        for (UpdateTicketTypeRequest ticketType : event.getTicketTypes()) {
            if (null == ticketType.getId()) {
                // Create
                TicketType ticketTypeToCreate = new TicketType();
                ticketTypeToCreate.setName(ticketType.getName());
                ticketTypeToCreate.setPrice(ticketType.getPrice());
                ticketTypeToCreate.setDescription(ticketType.getDescription());
                ticketTypeToCreate.setTotalAvailable(ticketType.getTotalAvailable());
                ticketTypeToCreate.setEvent(existingEvent);
                existingEvent.getTicketTypes().add(ticketTypeToCreate);

            } else if (existingTicketTypesIndex.containsKey(ticketType.getId())) {
                // Update
                TicketType existingTicketType = existingTicketTypesIndex.get(ticketType.getId());
                existingTicketType.setName(ticketType.getName());
                existingTicketType.setPrice(ticketType.getPrice());
                existingTicketType.setDescription(ticketType.getDescription());
                existingTicketType.setTotalAvailable(ticketType.getTotalAvailable());
            } else {
                throw new TicketTypeNotFoundException(String.format(
                        "Ticket type with ID '%s' does not exist", ticketType.getId()
                ));
            }
        }

        //save the entity data in db
        return eventRepository.save(existingEvent);
    }


    //trasnacational when multipe db calls are involved
    //for delete : only controller and service implemnetation is enough
    @Override
    @Transactional
    public void deleteEventForOrganizer(UUID organizerId, UUID eventId) {
        //fetch event from organizer from exiisting method and delete it if present
       getEventForOrganizer(organizerId, eventId).ifPresent(eventRepository::delete);
    }

    //-----------------------------------------------------------------------------------------------------------

    @Override
    public Page<Event> listPublishedEvents(Pageable pageable) {
        //return all events with enum type "PUBLISHED"
        return eventRepository.findByStatus(EventStatusEnum.PUBLISHED, pageable);
    }

    @Override
    public Page<Event> searchPublishedEvents(String query, Pageable pageable) {
        //search using query : event name , venue name etc.
        //custom query is made in repo class for postgres db
        return eventRepository.searchEvents(query, pageable);
    }

    //search using id in oublished event
    @Override
    public Optional<Event> getPublishedEvent(UUID id) {
        return eventRepository.findByIdAndStatus(id, EventStatusEnum.PUBLISHED);
    }

}

//updateEventForOrganizer()
//      │
//              ├── Validate IDs
//      ├── Fetch Event
//      ├── Update Event fields
//      ├── Remove deleted ticket types
//      ├── Build lookup map
//      ├── Create new ticket types
//      ├── Update existing ticket types
//      └── Transaction commits

