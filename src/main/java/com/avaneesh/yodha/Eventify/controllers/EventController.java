package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.EventRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.EventsResponse;
import com.avaneesh.yodha.Eventify.services.EventService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller for managing events.
 * Provides endpoints for creating, retrieving, updating, and deleting events.
 */
@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Endpoints for managing events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Creates a new event.
     *
     * @param request The details of the event to create.
     * @return A response entity containing the created event's details.
     */
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<EventsResponse>> createEvent(@Valid @ModelAttribute EventRequestDTO request) {
        EventsResponse event = eventService.createEvent(request);
        ApiResponse<EventsResponse> response = new ApiResponse<>(true, "Event created successfully", event);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a paginated list of all events.
     *
     * @param pageNo   The page number to retrieve (default is 0).
     * @param pageSize The number of events per page (default is 10).
     * @param sortBy   The field to sort the events by (default is "eventTimestamp").
     * @return A response entity containing the paginated list of events.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventsResponse>>> getAllEvents(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "eventTimestamp") String sortBy) {
        Page<EventsResponse> events = eventService.getAllEvents(pageNo, pageSize, sortBy);
        ApiResponse<Page<EventsResponse>> response = new ApiResponse<>(true, "Events fetched successfully", events);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<EventsResponse>>> searchEvents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "eventTimestamp") String sortBy) {

        Page<EventsResponse> events = eventService.searchEvents(
                name, category, startDate, endDate, pageNo, pageSize, sortBy);

        ApiResponse<Page<EventsResponse>> response = new ApiResponse<>(true, "Events fetched successfully", events);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific event by its ID.
     *
     * @param id The ID of the event to retrieve.
     * @return A response entity containing the event details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventsResponse>> getEventById(@PathVariable Long id) {
        EventsResponse event = eventService.getEventById(id);
        ApiResponse<EventsResponse> response = new ApiResponse<>(true, "Event fetched successfully", event);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific event by its ID.
     *
     * @param id The ID of the event to delete.
     * @return A response entity with no content, indicating successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Partially updates an existing event.
     *
     * @param id      The ID of the event to partially update.
     * @param request The partial event details.
     * @return A response entity containing the partially updated event's details.
     */
    @PatchMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, value = "/{id}")
    public ResponseEntity<ApiResponse<EventsResponse>> patchEvent(@PathVariable Long id, @ModelAttribute EventRequestDTO request) {
        EventsResponse patchedEvent = eventService.updateEvent(id, request);
        ApiResponse<EventsResponse> response = new ApiResponse<>(true, "Event patched successfully", patchedEvent);
        return ResponseEntity.ok(response);
    }
}
