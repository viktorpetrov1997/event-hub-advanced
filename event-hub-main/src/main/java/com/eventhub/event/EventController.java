package com.eventhub.event;

import com.eventhub.booking.BookingService;
import com.eventhub.booking.dto.BookingFormRequest;
import com.eventhub.event.dto.EventFormRequest;
import com.eventhub.event.exception.*;
import com.eventhub.security.AuthenticatedUser;
import com.eventhub.venue.VenueService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class EventController
{
    private final EventService eventService;
    private final VenueService venueService;
    private final BookingService bookingService;

    public EventController(EventService eventService, VenueService venueService, BookingService bookingService)
    {
        this.eventService = eventService;
        this.venueService = venueService;
        this.bookingService = bookingService;
    }

    @GetMapping("/events")
    public ModelAndView getEventsPage(@RequestParam(required = false) String search,
                                      @RequestParam(required = false) UUID venueId,
                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        UUID currentUserId = authenticatedUser.getId();
        String currentUserRole = authenticatedUser.getRole().name();

        List<Event> events = eventService.getVisibleEventsForUser(currentUserId, currentUserRole, search, venueId);

        Map<UUID, Integer> bookedSeatsByEvent = new HashMap<>();
        Map<UUID, Integer> availableSeatsByEvent = new HashMap<>();
        Map<UUID, Integer> percentBookedByEvent = new HashMap<>();

        for(Event event : events)
        {
            int bookedSeats = bookingService.getBookedSeatsForEvent(event.getId());
            int availableSeats = event.getCapacity() - bookedSeats;
            int percentBooked = event.getCapacity() == 0 ? 0 : (int) ((bookedSeats * 100.0) / event.getCapacity());

            bookedSeatsByEvent.put(event.getId(), bookedSeats);
            availableSeatsByEvent.put(event.getId(), availableSeats);
            percentBookedByEvent.put(event.getId(), percentBooked);
        }

        ModelAndView modelAndView = new ModelAndView("events");
        modelAndView.addObject("events", events);
        modelAndView.addObject("venues", venueService.getAllVenues());
        modelAndView.addObject("search", search);
        modelAndView.addObject("venueId", venueId);
        modelAndView.addObject("bookedSeatsByEvent", bookedSeatsByEvent);
        modelAndView.addObject("availableSeatsByEvent", availableSeatsByEvent);
        modelAndView.addObject("percentBookedByEvent", percentBookedByEvent);

        return modelAndView;
    }

    @GetMapping("/event-form")
    public ModelAndView getEventFormPage()
    {
        EventFormRequest eventForm = new EventFormRequest();

        ModelAndView modelAndView = new ModelAndView("event-form");
        modelAndView.addObject("eventForm", eventForm);
        modelAndView.addObject("venues", venueService.getAllVenues());
        return modelAndView;
    }

    @PostMapping("/events")
    public ModelAndView createEvent(@Valid @ModelAttribute("eventForm") EventFormRequest eventForm,
                                    BindingResult bindingResult, @AuthenticationPrincipal AuthenticatedUser authenticatedUser, RedirectAttributes redirectAttributes)
    {
        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("event-form");
            modelAndView.addObject("eventForm", eventForm);
            modelAndView.addObject("venues", venueService.getAllVenues());
            return modelAndView;
        }

        UUID organizerId = authenticatedUser.getId();

        try
        {
            eventService.createEvent(eventForm, organizerId);

            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully.");

            return new ModelAndView("redirect:/events");
        }
        catch(EventCapacityExceedsVenueException ex)
        {
            bindingResult.rejectValue("capacity", "event.capacity.exceedsVenue", ex.getMessage());
        }
        catch(VenueAlreadyBookedException ex)
        {
            bindingResult.rejectValue("venueId", "event.venue.alreadyBooked", ex.getMessage());
        }

        ModelAndView modelAndView = new ModelAndView("event-form");
        modelAndView.addObject("eventForm", eventForm);
        modelAndView.addObject("venues", venueService.getAllVenues());
        return modelAndView;
    }

    @GetMapping("/events/{id}")
    public ModelAndView getEventDetails(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        UUID currentUserId = authenticatedUser.getId();

        Event event = eventService.getEventById(id);

        boolean isOwner = event.getOrganizer() != null
                && event.getOrganizer().getId().equals(currentUserId);

        boolean alreadyBooked = bookingService.hasConfirmedBooking(currentUserId, id);

        if(!event.isPublished() && !isOwner && !alreadyBooked)
        {
            return new ModelAndView("redirect:/events");
        }

        int bookedSeats = bookingService.getBookedSeatsForEvent(id);
        int availableSeats = event.getCapacity() - bookedSeats;

        boolean hasActiveBookings = bookingService.hasActiveBookings(id);

        BookingFormRequest bookingForm = new BookingFormRequest();

        ModelAndView modelAndView = new ModelAndView("event-details");
        modelAndView.addObject("event", event);
        modelAndView.addObject("bookingForm", bookingForm);
        modelAndView.addObject("isOwner", isOwner);
        modelAndView.addObject("alreadyBooked", alreadyBooked);
        modelAndView.addObject("availableSeats", availableSeats);
        modelAndView.addObject("hasActiveBookings", hasActiveBookings);

        return modelAndView;
    }

    @GetMapping("/events/{id}/edit")
    public ModelAndView getEventEditPage(@PathVariable UUID id,
                                         @RequestParam(defaultValue = "details") String redirectTo,
                                         @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                         RedirectAttributes redirectAttributes)
    {
        Event event = eventService.getEventById(id);

        UUID currentUserId = authenticatedUser.getId();

        boolean isOwner = event.getOrganizer() != null
                && event.getOrganizer().getId().equals(currentUserId);

        if(!isOwner)
        {
            return new ModelAndView("redirect:/events");
        }

        if(!event.getStartTime().isAfter(LocalDateTime.now()))
        {
            redirectAttributes.addFlashAttribute("errorMessage", "This event has already started and can no longer be edited.");

            return new ModelAndView("redirect:/events/" + id);
        }

        EventFormRequest eventForm = new EventFormRequest();

        eventForm.setTitle(event.getTitle());
        eventForm.setDescription(event.getDescription());
        eventForm.setStartTime(event.getStartTime());
        eventForm.setEndTime(event.getEndTime());
        eventForm.setPrice(event.getPrice());
        eventForm.setCapacity(event.getCapacity());
        eventForm.setPublished(event.isPublished());
        eventForm.setVenueId(event.getVenue().getId());

        ModelAndView modelAndView = new ModelAndView("event-form");
        modelAndView.addObject("eventForm", eventForm);
        modelAndView.addObject("eventId", id);
        modelAndView.addObject("redirectTo", redirectTo);
        modelAndView.addObject("venues", venueService.getAllVenues());
        return modelAndView;
    }

    @PutMapping("/events/{id}")
    public ModelAndView updateEvent(@PathVariable UUID id,
                                    @Valid @ModelAttribute("eventForm") EventFormRequest eventForm,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                    @RequestParam(defaultValue = "details") String redirectTo,
                                    RedirectAttributes redirectAttributes)
    {
        UUID currentUserId = authenticatedUser.getId();

        Event event = eventService.getEventById(id);

        if(!event.getOrganizer().getId().equals(currentUserId))
        {
            return new ModelAndView("redirect:/events");
        }

        if(!event.getStartTime().isAfter(LocalDateTime.now()))
        {
            redirectAttributes.addFlashAttribute("errorMessage", "This event has already started and can no longer be edited.");

            return new ModelAndView("redirect:/events/" + id);
        }

        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("event-form");
            modelAndView.addObject("eventForm", eventForm);
            modelAndView.addObject("eventId", id);
            modelAndView.addObject("redirectTo", redirectTo);
            modelAndView.addObject("venues", venueService.getAllVenues());
            return modelAndView;
        }

        try
        {
            eventService.updateEvent(id, eventForm);

            if(redirectTo.equals("dashboard"))
            {
                return new ModelAndView("redirect:/dashboard");
            }

            return new ModelAndView("redirect:/events/" + id);
        }
        catch(EventAlreadyStartedException ex)
        {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return new ModelAndView("redirect:/events/" + id);
        }
        catch(EventCapacityExceedsVenueException ex)
        {
            bindingResult.rejectValue("capacity", "event.capacity.exceedsVenue", ex.getMessage());
        }
        catch(VenueAlreadyBookedException ex)
        {
            bindingResult.rejectValue("venueId", "event.venue.alreadyBooked", ex.getMessage());
        }
        catch(EventCapacityBelowBookedSeatsException ex)
        {
            bindingResult.rejectValue("capacity", "event.capacity.belowBooked", ex.getMessage());
        }

        ModelAndView modelAndView = new ModelAndView("event-form");
        modelAndView.addObject("eventForm", eventForm);
        modelAndView.addObject("eventId", id);
        modelAndView.addObject("redirectTo", redirectTo);
        modelAndView.addObject("venues", venueService.getAllVenues());
        return modelAndView;
    }

    @PutMapping("/events/{id}/visibility")
    public ModelAndView toggleEventVisibility(@PathVariable UUID id,
                                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                              RedirectAttributes redirectAttributes,
                                              @RequestParam(defaultValue = "details") String redirectTo)
    {
        UUID currentUserId = authenticatedUser.getId();

        Event event = eventService.getEventById(id);

        if(!event.getOrganizer().getId().equals(currentUserId))
        {
            return new ModelAndView("redirect:/events");
        }

        try
        {
            eventService.togglePublished(id);

            String message = eventService.getEventById(id).isPublished()
                    ? "Event published successfully."
                    : "Event hidden successfully.";

            redirectAttributes.addFlashAttribute("successMessage", message);
        }
        catch(EventAlreadyEndedException ex)
        {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if(redirectTo.equals("dashboard"))
        {
            return new ModelAndView("redirect:/dashboard");
        }

        return new ModelAndView("redirect:/events/" + id);
    }

    @DeleteMapping("/events/{id}")
    public ModelAndView deleteEvent(@PathVariable UUID id,
                                    @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                    @RequestParam(defaultValue = "events") String redirectTo,
                                    RedirectAttributes redirectAttributes)
    {
        UUID currentUserId = authenticatedUser.getId();

        Event event = eventService.getEventById(id);

        if(!event.getOrganizer().getId().equals(currentUserId))
        {
            return new ModelAndView("redirect:/events");
        }

        try
        {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
        }
        catch(EventHasActiveBookingsException ex)
        {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

            if(redirectTo.equals("dashboard"))
            {
                return new ModelAndView("redirect:/dashboard");
            }
            else
            {
                return new ModelAndView("redirect:/events/" + id);
            }
        }

        if(redirectTo.equals("dashboard"))
        {
            return new ModelAndView("redirect:/dashboard");
        }

        return new ModelAndView("redirect:/events");
    }

    @GetMapping("/dashboard")
    public ModelAndView getDashboard(@AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        UUID organizerId = authenticatedUser.getId();

        List<Event> events = eventService.getOrganizerEvents(organizerId);

        Map<UUID, Integer> bookedSeatsByEvent = new HashMap<>();
        Map<UUID, Boolean> hasActiveBookingsByEvent = new HashMap<>();

        for(Event event : events)
        {
            int bookedSeats = bookingService.getBookedSeatsForEvent(event.getId());
            bookedSeatsByEvent.put(event.getId(), bookedSeats);

            boolean hasActiveBookings = bookingService.hasActiveBookings(event.getId());
            hasActiveBookingsByEvent.put(event.getId(), hasActiveBookings);
        }

        int totalEvents = events.size();

        long publishedEvents = events.stream()
                .filter(Event::isPublished)
                .count();

        int soldSeats = bookedSeatsByEvent.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal revenue = bookingService.getRevenueForOrganizer(organizerId);

        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("events", events);
        modelAndView.addObject("bookedSeatsByEvent", bookedSeatsByEvent);
        modelAndView.addObject("hasActiveBookingsByEvent", hasActiveBookingsByEvent);
        modelAndView.addObject("totalEvents", totalEvents);
        modelAndView.addObject("publishedEvents", publishedEvents);
        modelAndView.addObject("soldSeats", soldSeats);
        modelAndView.addObject("revenue", revenue);

        return modelAndView;
    }
}
