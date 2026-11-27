package com.eventhub.event;

import com.eventhub.booking.BookingService;
import com.eventhub.event.dto.EventFormRequest;
import com.eventhub.event.exception.*;
import com.eventhub.user.User;
import com.eventhub.user.UserRepository;
import com.eventhub.user.exception.UserNotFoundException;
import com.eventhub.venue.Venue;
import com.eventhub.venue.VenueRepository;
import com.eventhub.venue.exception.VenueNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class EventService
{
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository, UserRepository userRepository, BookingService bookingService)
    {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
    }

    public List<Event> getVisibleEventsForUser(UUID currentUserId, String currentUserRole, String search, UUID venueId)
    {
        LocalDateTime now = LocalDateTime.now();

        List<Event> events = "ADMIN".equals(currentUserRole)
                ? eventRepository.findAllByEndTimeAfterOrderByStartTimeAsc(now)
                : eventRepository.findAllByPublishedTrueAndEndTimeAfterOrderByStartTimeAsc(now);

        if("ORGANIZER".equals(currentUserRole) && currentUserId != null)
        {
            List<Event> organizerEvents = eventRepository.findAllByOrganizerIdAndEndTimeAfter(currentUserId, now);
            events = Stream.concat(events.stream(), organizerEvents.stream())
                    .distinct()
                    .sorted(Comparator.comparing(Event::getStartTime))
                    .toList();
        }

        if(search != null && !search.isBlank())
        {
            String keyword = search.toLowerCase();
            events = events.stream()
                    .filter(event ->
                            event.getTitle().toLowerCase().contains(keyword)
                                    || event.getDescription().toLowerCase().contains(keyword))
                    .toList();
        }

        if(venueId != null)
        {
            events = events.stream()
                    .filter(event -> event.getVenue() != null && event.getVenue().getId().equals(venueId))
                    .toList();
        }

        return events;
    }

    public void createEvent(EventFormRequest request, UUID organizerId)
    {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Venue venue = venueRepository.findByIdAndActiveTrue(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        validateVenueConstraints(request, venue, null);

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .published(request.isPublished())
                .venue(venue)
                .organizer(organizer)
                .build();

        eventRepository.save(event);
    }

    public Event getEventById(UUID id)
    {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    }

    public void updateEvent(UUID id, EventFormRequest request)
    {
        Event event = getEventById(id);

        if(!event.getStartTime().isAfter(LocalDateTime.now()))
        {
            throw new EventAlreadyStartedException("This event has already started and can no longer be edited.");
        }

        Venue venue = venueRepository.findByIdAndActiveTrue(request.getVenueId())
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        validateVenueConstraints(request, venue, id);

        int bookedSeats = bookingService.getBookedSeatsForEvent(id);

        if(request.getCapacity() < bookedSeats)
        {
            throw new EventCapacityBelowBookedSeatsException(
                    "Capacity cannot be lower than the " + bookedSeats + " seats already booked.");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setPrice(request.getPrice());
        event.setCapacity(request.getCapacity());
        event.setPublished(request.isPublished());
        event.setVenue(venue);

        eventRepository.save(event);
    }

    private void validateVenueConstraints(EventFormRequest request, Venue venue, UUID excludeEventId)
    {
        if(request.getCapacity() > venue.getCapacity())
        {
            throw new EventCapacityExceedsVenueException(
                    "Capacity cannot exceed the venue capacity (" + venue.getCapacity() + " seats).");
        }

        List<Event> overlapping = eventRepository.findOverlappingEventsAtVenue(
                venue.getId(), request.getStartTime(), request.getEndTime(), excludeEventId);

        if(!overlapping.isEmpty())
        {
            Event conflict = overlapping.get(0);

            throw new VenueAlreadyBookedException(
                    "This venue is already taken by " + conflict.getTitle() + " from "
                            + conflict.getStartTime().format(DATE_TIME_FORMAT) + " to "
                            + conflict.getEndTime().format(DATE_TIME_FORMAT) + ".");
        }
    }

    public void togglePublished(UUID id)
    {
        Event event = getEventById(id);

        if(!event.getEndTime().isAfter(LocalDateTime.now()))
        {
            throw new EventAlreadyEndedException("You cannot change the visibility of an event that has already ended.");
        }

        event.setPublished(!event.isPublished());

        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(UUID id)
    {
        Event event = getEventById(id);

        if(bookingService.hasActiveBookings(id))
        {
            throw new EventHasActiveBookingsException("An event with active bookings cannot be deleted.");
        }

        bookingService.deleteAllBookingsForEvent(id);

        eventRepository.delete(event);
    }

    public List<Event> getOrganizerEvents(UUID organizerId)
    {
        return eventRepository.findAllByOrganizerIdOrderByStartTimeAsc(organizerId);
    }
}
