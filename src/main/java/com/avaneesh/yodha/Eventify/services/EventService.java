package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.EventRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.EventsResponse;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Seat;
import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.EventMapper;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.SeatRepository;
import com.avaneesh.yodha.Eventify.repository.specifications.EventSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for managing events, including creation, retrieval, updates, and seat generation.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final EventMapper eventMapper;
    private final StorageService storageService;

    public EventService(EventRepository eventRepository, SeatRepository seatRepository, EventMapper eventMapper, StorageService storageService) {
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.eventMapper = eventMapper;
        this.storageService = new StorageService();
    }

    /**
     * Creates a new event and generates its seating arrangement.
     *
     * @param request The DTO containing the event details.
     * @return A DTO representing the newly created event.
     */
    @Transactional
    public EventsResponse createEvent(EventRequestDTO request) {
        Events newEvent = eventMapper.toEvent(request);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imageUrls = storageService.saveFiles(request.getImages());
            newEvent.setImageUrls(imageUrls);
        }
        Events savedEvent = eventRepository.save(newEvent);

        List<Seat> seats = generateSeatsForEvent(savedEvent, request);
        seatRepository.saveAll(seats);
        savedEvent.setSeats(seats);

        return eventMapper.toEventResponse(savedEvent);
    }

    /**
     * Retrieves a paginated list of all events.
     *
     * @param pageNo   The page number.
     * @param pageSize The size of the page.
     * @param sortBy   The field to sort by.
     * @return A paginated list of event DTOs.
     */
    public Page<EventsResponse> getAllEvents(int pageNo, int pageSize, String sortBy) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        return eventRepository.findAll(pageable).map(eventMapper::toEventResponse);
    }

    public Page<EventsResponse> searchEvents(
            String name, String category, LocalDateTime startDate, LocalDateTime endDate,
            int pageNo, int pageSize, String sortBy, Double minPrice, Double maxPrice) {

        Specification<Events> spec = EventSpecification.getEvents(name, category, startDate, endDate, minPrice, maxPrice);
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));

        return eventRepository.findAll(spec, pageable).map(eventMapper::toEventResponse);
    }

    /**
     * Retrieves a single event by its ID.
     *
     * @param eventId The ID of the event.
     * @return A DTO representing the event.
     */
    public EventsResponse getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .map(eventMapper::toEventResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    /**
     * Updates an existing event. If the seating layout has changed and there are no existing bookings,
     * the old seats are deleted and new ones are generated.
     *
     * @param eventId The ID of the event to update.
     * @param request The DTO with updated event details.
     * @return A DTO representing the updated event.
     * @throws IllegalStateException if attempting to change the seat layout of an event that already has bookings.
     */
    @Transactional
    public EventsResponse updateEvent(Long eventId, EventRequestDTO request) {
        // 1. Existing event ko database se fetch karein
        Events existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // ... seat layout change karne ka logic waisa hi rahega ...
        boolean layoutChanged = hasLayoutChanged(existingEvent, request);
        if (layoutChanged) {
            if (existingEvent.getBookedSeats() > 0) {
                throw new IllegalStateException("Cannot change seat layout for an event that already has bookings.");
            }
            seatRepository.deleteByEvent(existingEvent);
            existingEvent.getSeats().clear();
            List<Seat> newSeats = generateSeatsForEvent(existingEvent, request);
            existingEvent.getSeats().addAll(newSeats);
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
             storageService.deleteFiles(existingEvent.getImageUrls());
            List<String> newImageUrls = storageService.saveFiles(request.getImages());
            existingEvent.setImageUrls(newImageUrls);
        }
        eventMapper.updateEventFromDto(request, existingEvent);

        Events updatedEvent = eventRepository.save(existingEvent);
        return eventMapper.toEventResponse(updatedEvent);
    }

    /**
     * Deletes an event by its ID.
     *
     * @param id The ID of the event to delete.
     */
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
    }

    // --- Private Helper Methods ---

    private List<Seat> generateSeatsForEvent(Events event, EventRequestDTO request) {
        int totalSeats = request.getTotalSeats();
        int seatsPerRow = request.getSeatsPerRow();
        List<Double> seatPricing = request.getSeatPricing();
        int totalRows = (int) Math.ceil((double) totalSeats / seatsPerRow);

        if (seatPricing.size() != totalRows) {
            throw new IllegalArgumentException("The number of prices in seatPricing (" + seatPricing.size() +
                    ") must match the calculated number of rows (" + totalRows + ").");
        }

        List<Seat> seats = new ArrayList<>();
        char currentRowChar = 'A';
        int currentSeatInRow = 1;
        int rowIndex = 0;

        for (int i = 0; i < totalSeats; i++) {
            String seatNumber = String.format("%c%d", currentRowChar, currentSeatInRow);
            double currentPrice = seatPricing.get(rowIndex);

            Seat seat = new Seat();
            seat.setSeatNumber(seatNumber);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setEvent(event);
            seat.setSeatPricing(currentPrice);
            seats.add(seat);

            currentSeatInRow++;
            if (currentSeatInRow > seatsPerRow) {
                currentSeatInRow = 1;
                currentRowChar++;
                rowIndex++;
            }
        }
        return seats;
    }

    private boolean hasLayoutChanged(Events event, EventRequestDTO request) {
        int currentTotalSeats = event.getSeats() != null ? event.getSeats().size() : 0;
        if (currentTotalSeats == 0) return true; // Layout is new

        // A simple way to estimate original seats per row
        int currentSeatsPerRow = event.getSeats().stream()
                .filter(s -> s.getSeatNumber().startsWith("A"))
                .mapToInt(s -> 1)
                .sum();

        return request.getTotalSeats() != currentTotalSeats || request.getSeatsPerRow() != currentSeatsPerRow;
    }
}
