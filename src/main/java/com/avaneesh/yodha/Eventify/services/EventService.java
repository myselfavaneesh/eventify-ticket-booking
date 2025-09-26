package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.CreateEventRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.EventsResponse;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Seat;
import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.EventMapper;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventMapper eventMapper;

    @Transactional
    public Events createEvent(CreateEventRequestDTO request) {

        Events savedEvent = eventRepository.save(eventMapper.toEvent(request));

        List<Seat> seats = new ArrayList<>();
        char currentRow = 'A';
        int currentSeatInRow = 1;

        for (int i = 0; i < request.getTotalSeats(); i++) {
            // Generate the seat number, e.g., "A1", "A2"
            String seatNumber = currentRow + "" + currentSeatInRow;

            Seat seat = new Seat();
            seat.setSeatNumber(seatNumber);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setEvent(savedEvent);
            seats.add(seat);

            // Move to the next seat in the row
            currentSeatInRow++;

            // If the row is full, move to the next row and reset seat count
            if (currentSeatInRow > request.getSeatsPerRow()) {
                currentSeatInRow = 1;
                currentRow++;
            }
        }

        seatRepository.saveAll(seats);

        return savedEvent;
    }

    @Transactional
    public Page<EventsResponse> getAllEvents(int pageNo, int pageSize, String sortBy) {
        // Create the Pageable object with pagination and sorting
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));

        // Fetch the paginated Events entities from the repository
        Page<Events> eventsPage = eventRepository.findAll(pageable);

        // Map the paginated Events entities to EventsResponse DTOs
        return eventsPage.map(eventMapper::toEventResponse);
    }

    @Transactional
    public Page<EventsResponse> getAllSpecificVenueEvents(String venue, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Events> eventsPage = eventRepository.findByVenueContaining(venue ,pageable);
        return eventsPage.map(eventMapper::toEventResponse);
    }

    @Transactional
    public EventsResponse getEventById(Long eventId) {
        Events ReqEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found with id: " + eventId)
        );

        return eventMapper.toEventResponse(ReqEvent);
    }

    @Transactional
    public EventsResponse updateEvent(Long eventId, CreateEventRequestDTO request) {
        Events existingEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new ResourceNotFoundException("Event not found with id: " + eventId)
        );

        // Update event details
        existingEvent.setName(request.getName());
        existingEvent.setDescription(request.getDescription());
        existingEvent.setVenue(request.getVenue());
        existingEvent.setEventTimestamp(LocalDateTime.parse(request.getEventTimestamp()));

        // If totalSeats changed → rebuild seats
        if (request.getTotalSeats() != existingEvent.getSeats().size()) {
            // Clear old seats (orphanRemoval = true will delete them)
            existingEvent.getSeats().clear();

            List<Seat> newSeats = new ArrayList<>();
            char currentRow = 'A';
            int currentSeatInRow = 1;

            for (int i = 0; i < request.getTotalSeats(); i++) {
                String seatNumber = currentRow + "" + currentSeatInRow;

                Seat seat = new Seat();
                seat.setSeatNumber(seatNumber);
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setEvent(existingEvent);   // link to event
                newSeats.add(seat);

                currentSeatInRow++;
                if (currentSeatInRow > request.getSeatsPerRow()) {
                    currentSeatInRow = 1;
                    currentRow++;
                }
            }

            existingEvent.getSeats().addAll(newSeats);
        }

        Events updatedEvent = eventRepository.save(existingEvent);
        return eventMapper.toEventResponse(updatedEvent);
    }


    @Transactional
    public void deleteEvent(Long id) {
        Events existingEvent = eventRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Event not found with id: " + id));
        eventRepository.delete(existingEvent);
    }
}
