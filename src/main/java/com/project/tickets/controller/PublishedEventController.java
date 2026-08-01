package com.project.tickets.controller;

import com.project.tickets.domain.dtos.GetPublishedEventDetailsResponseDto;
import com.project.tickets.domain.dtos.ListEventResponseDto;
import com.project.tickets.domain.entities.Event;
import com.project.tickets.mappers.EventMapper;
import com.project.tickets.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//treat all published events seprately as it is seprate tasks so make different controler classes
//service methods are in events only
//no authorization for these end points

@RestController
@RequestMapping(path = "/api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {

    private final EventService eventService;
    private final EventMapper eventMapper;

//in security config , permit accessing for this end point

    @GetMapping
    public ResponseEntity<Page<ListEventResponseDto>> listPublishedEvents(
            @RequestParam(required = false) String q,
            Pageable pageable) {

        Page<Event> events;
        if (null != q && !q.trim().isEmpty()) {
            events = eventService.searchPublishedEvents(q, pageable);
        } else {
            //if query not provided list all published evengts diretcly
            //query is optional
            events = eventService.listPublishedEvents(pageable);
        }

        return ResponseEntity.ok(
                events.map(eventMapper::toListPublishedEventResponseDto)
        );
    }

    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GetPublishedEventDetailsResponseDto> getPublishedEventDetails(
            @PathVariable UUID eventId
    ) {
        return eventService.getPublishedEvent(eventId)
                //map to toGetPublishedEventDetailsResponseDto
                //return resposne entity ok else not found
                .map(eventMapper::toGetPublishedEventDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
