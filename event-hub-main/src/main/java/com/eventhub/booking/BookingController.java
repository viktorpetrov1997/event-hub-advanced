package com.eventhub.booking;

import com.eventhub.booking.dto.BookingFormRequest;
import com.eventhub.booking.exception.BookingAccessDeniedException;
import com.eventhub.booking.exception.BookingCancellationNotAllowedException;
import com.eventhub.booking.exception.BookingNotAllowedException;
import com.eventhub.booking.exception.NotEnoughSeatsException;
import com.eventhub.event.Event;
import com.eventhub.event.EventService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
public class BookingController
{
    private final BookingService bookingService;
    private final EventService eventService;

    public BookingController(BookingService bookingService, EventService eventService)
    {
        this.bookingService = bookingService;
        this.eventService = eventService;
    }

    @GetMapping("/my-bookings")
    public ModelAndView getMyBookings(HttpSession httpSession)
    {
        UUID userId = (UUID) httpSession.getAttribute("user_id");

        List<Booking> bookings = bookingService.getBookingsForUser(userId);

        ModelAndView modelAndView = new ModelAndView("my-bookings");
        modelAndView.addObject("bookings", bookings);
        return modelAndView;
    }

    @PostMapping("/events/{id}/book")
    public ModelAndView bookEvent(@PathVariable UUID id,
                                  @Valid @ModelAttribute("bookingForm") BookingFormRequest bookingForm,
                                  BindingResult bindingResult,
                                  HttpSession httpSession,
                                  RedirectAttributes redirectAttributes)
    {
        UUID userId = (UUID) httpSession.getAttribute("user_id");

        if(bindingResult.hasErrors())
        {
            return buildEventDetailsView(id, userId, bookingForm);
        }

        try
        {
            bookingService.createBooking(userId, id, bookingForm);
        }
        catch(BookingNotAllowedException ex)
        {
            ModelAndView modelAndView = buildEventDetailsView(id, userId, bookingForm);
            modelAndView.addObject("errorMessage", ex.getMessage());
            return modelAndView;
        }
        catch(NotEnoughSeatsException ex)
        {
            bindingResult.rejectValue("seats", "booking.seats.notEnough", ex.getMessage());
            return buildEventDetailsView(id, userId, bookingForm);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Your booking is confirmed.");

        return new ModelAndView("redirect:/events/" + id);
    }

    private ModelAndView buildEventDetailsView(UUID eventId, UUID currentUserId, BookingFormRequest bookingForm)
    {
        Event event = eventService.getEventById(eventId);

        ModelAndView modelAndView = new ModelAndView("event-details");
        modelAndView.addObject("event", event);
        modelAndView.addObject("bookingForm", bookingForm);
        modelAndView.addObject("isOwner", event.getOrganizer().getId().equals(currentUserId));
        modelAndView.addObject("alreadyBooked", bookingService.hasConfirmedBooking(currentUserId, eventId));
        modelAndView.addObject("availableSeats", event.getCapacity() - bookingService.getBookedSeatsForEvent(eventId));
        modelAndView.addObject("hasActiveBookings", bookingService.hasActiveBookings(eventId));
        return modelAndView;
    }

    @PutMapping("/bookings/{id}/cancel")
    public ModelAndView cancelBooking(@PathVariable UUID id, HttpSession httpSession,
                                      RedirectAttributes redirectAttributes)
    {
        UUID userId = (UUID) httpSession.getAttribute("user_id");

        try
        {
            bookingService.cancelBooking(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Your booking has been canceled.");
        }
        catch(BookingAccessDeniedException | BookingCancellationNotAllowedException ex)
        {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return new ModelAndView("redirect:/my-bookings");
    }
}
