package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.EventRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.EventsResponse;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.services.EventService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;


    @PostMapping
    public ResponseEntity<ApiResponse<Events>> createEvent(@Valid @RequestBody EventRequestDTO request) {
        Events event = eventService.createEvent(request);
        return new ResponseEntity<>(new ApiResponse<>(true,"Event created successfully", event), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventsResponse>>> getAllEvents(@RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "10") int pageSize) {
        Page<EventsResponse> events = eventService.getAllEvents(pageNo,pageSize,"eventTimestamp");
        return new ResponseEntity<>(new ApiResponse<>(true,"Events fetched successfully", events), HttpStatus.OK);
    }

    @GetMapping("/venue/{venue}")
    public ResponseEntity<ApiResponse<Page<EventsResponse>>> getAllSpecificVenueEvents(@PathVariable String venue,@RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "10") int pageSize) {
        Page<EventsResponse> events = eventService.getAllSpecificVenueEvents(venue,pageNo,pageSize);
        return new ResponseEntity<>(new ApiResponse<>(true,"Events for venue " + venue + " fetched successfully", events), HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<EventsResponse>> getEventById(@PathVariable Long id) {
        EventsResponse event = eventService.getEventById(id);
        return new ResponseEntity<>(new ApiResponse<>(true,"Event fetched successfully", event), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return new ResponseEntity<>(new ApiResponse<>(true, "Event deleted successfully", null), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventsResponse>> updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequestDTO request) {
        EventsResponse updatedEvent = eventService.updateEvent(id, request);
        return new ResponseEntity<>(new ApiResponse<>(true, "Event updated successfully", updatedEvent), HttpStatus.OK);
    }


}