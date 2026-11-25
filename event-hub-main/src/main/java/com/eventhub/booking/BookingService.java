package com.eventhub.booking;

import com.eventhub.booking.dto.BookingFormRequest;
import com.eventhub.booking.exception.*;
import com.eventhub.event.Event;
import com.eventhub.event.EventRepository;
import com.eventhub.event.exception.EventNotFoundException;
import com.eventhub.user.User;
import com.eventhub.user.UserRepository;
import com.eventhub.user.UserRole;
import com.eventhub.user.exception.UserNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService
{
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository, UserRepository userRepository)
    {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<Booking> getBookingsForUser(UUID userId)
    {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void createBooking(UUID userId, UUID eventId, BookingFormRequest request)
    {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getRole() != UserRole.ATTENDEE)
        {
            throw new BookingNotAllowedException("Only attendees can book events.");
        }

        if(!event.isPublished())
        {
            throw new BookingNotAllowedException("This event is not available for booking.");
        }

        if(!event.getStartTime().isAfter(LocalDateTime.now()))
        {
            throw new BookingNotAllowedException("Your booking was not completed because the event has just started.");
        }

        if(hasConfirmedBooking(userId, eventId))
        {
            throw new BookingNotAllowedException("You have already booked this event.");
        }

        int requestedSeats = request.getSeats();

        int bookedSeats = getBookedSeatsForEvent(eventId);
        int availableSeats = event.getCapacity() - bookedSeats;

        if(requestedSeats > availableSeats)
        {
            throw new NotEnoughSeatsException(availableSeats <= 0
                    ? "This event is sold out."
                    : "Only " + availableSeats + " seat(s) left.");
        }

        Booking booking = Booking.builder()
                .seats(requestedSeats)
                .totalPrice(event.getPrice().multiply(BigDecimal.valueOf(requestedSeats)))
                .status(BookingStatus.CONFIRMED)
                .event(event)
                .user(user)
                .build();

        bookingRepository.save(booking);
    }

    public void cancelBooking(UUID bookingId, UUID userId)
    {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if(!booking.getUser().getId().equals(userId))
        {
            throw new BookingAccessDeniedException("You cannot cancel someone else's booking.");
        }

        if(booking.getStatus() != BookingStatus.CONFIRMED)
        {
            throw new BookingCancellationNotAllowedException("Only confirmed bookings can be cancelled.");
        }

        if(!booking.getEvent().getStartTime().isAfter(LocalDateTime.now()))
        {
            throw new BookingCancellationNotAllowedException("This event has already started and can no longer be cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }

    public int getBookedSeatsForEvent(UUID eventId)
    {
        return bookingRepository.sumBookedSeatsByEventId(eventId);
    }

    public boolean hasConfirmedBooking(UUID userId, UUID eventId)
    {
        return bookingRepository.existsByUserIdAndEventIdAndStatus(userId, eventId, BookingStatus.CONFIRMED);
    }

    public boolean hasActiveBookings(UUID eventId)
    {
        return bookingRepository.existsByEventIdAndStatusNot(eventId, BookingStatus.CANCELLED);
    }

    @Transactional
    public void deleteAllBookingsForEvent(UUID eventId)
    {
        bookingRepository.deleteAllByEventId(eventId);
    }

    public BigDecimal getRevenueForOrganizer(UUID organizerId)
    {
        return bookingRepository.sumRevenueByOrganizerId(organizerId);
    }
}
