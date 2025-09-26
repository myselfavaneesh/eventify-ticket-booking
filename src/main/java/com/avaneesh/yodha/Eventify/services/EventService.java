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
    public Events createEvent(EventRequestDTO request) {

        // --- Validation Step ---
        int totalRows = (int) Math.ceil((double) request.getTotalSeats() / request.getSeatsPerRow());
        if (request.getSeatPricing().size() != totalRows) {
            throw new IllegalArgumentException("The number of prices in seatPricing (" + request.getSeatPricing().size() +
                    ") must match the total number of rows (" + totalRows + ").");
        }
        // --- End Validation ---

        Events savedEvent = eventRepository.save(eventMapper.toEvent(request));

        List<Seat> seats = new ArrayList<>();
        char currentRowChar = 'A';
        int currentSeatInRow = 1;
        int rowIndex = 0; // To track which price to use from the list

        for (int i = 0; i < request.getTotalSeats(); i++) {
            String seatNumber = currentRowChar + "" + currentSeatInRow;
            Double currentPrice = request.getSeatPricing().get(rowIndex);

            Seat seat = new Seat();
            seat.setSeatNumber(seatNumber);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setEvent(savedEvent);
            seat.setSeatPricing(currentPrice);
            seats.add(seat);

            currentSeatInRow++;
            if (currentSeatInRow > request.getSeatsPerRow()) {
                currentSeatInRow = 1;
                currentRowChar++;
                rowIndex++;
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
    public EventsResponse updateEvent(Long eventId, EventRequestDTO request) {
        Events existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Update basic event details
        existingEvent.setName(request.getName());
        existingEvent.setDescription(request.getDescription());
        existingEvent.setVenue(request.getVenue());
        existingEvent.setEventTimestamp(LocalDateTime.parse(request.getEventTimestamp()));

        // --- Logic to check if seats need rebuilding ---
        // Calculate the original number of rows for comparison
        int originalTotalSeats = existingEvent.getSeats().size();
        int originalSeatsPerRow = 0;
        if (!existingEvent.getSeats().isEmpty()) {
            // A simple way to estimate original seats per row
            String lastSeatNumber = existingEvent.getSeats().get(originalTotalSeats - 1).getSeatNumber();
            originalSeatsPerRow = Integer.parseInt(lastSeatNumber.substring(1));
        }

        boolean seatsChanged = request.getTotalSeats() != originalTotalSeats ||
                request.getSeatsPerRow() != originalSeatsPerRow ||
                request.getSeatPricing().size() != (int) Math.ceil((double) request.getTotalSeats() / request.getSeatsPerRow());


        if (seatsChanged) {
            // --- Validation Step ---
            int totalRows = (int) Math.ceil((double) request.getTotalSeats() / request.getSeatsPerRow());
            if (request.getSeatPricing().size() != totalRows) {
                throw new IllegalArgumentException("The number of prices in seatPricing (" + request.getSeatPricing().size() +
                        ") must match the total number of rows (" + totalRows + ").");
            }
            // --- End Validation ---

            // Clear old seats before adding new ones
            existingEvent.getSeats().clear();
            seatRepository.deleteByEvent(existingEvent); // Explicitly delete old seats to be safe

            List<Seat> newSeats = new ArrayList<>();
            char currentRowChar = 'A';
            int currentSeatInRow = 1;
            int rowIndex = 0; // To track which price to use

            for (int i = 0; i < request.getTotalSeats(); i++) {
                String seatNumber = currentRowChar + "" + currentSeatInRow;
                Double currentPrice = request.getSeatPricing().get(rowIndex); // Get price for the current row

                Seat seat = new Seat();
                seat.setSeatNumber(seatNumber);
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setEvent(existingEvent);
                seat.setSeatPricing(currentPrice); // Set the price
                newSeats.add(seat);

                currentSeatInRow++;
                if (currentSeatInRow > request.getSeatsPerRow()) {
                    currentSeatInRow = 1;
                    currentRowChar++;
                    rowIndex++; // Move to the next price for the next row
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
