package com.eventhub.venue;

import com.eventhub.venue.dto.VenueFormRequest;
import com.eventhub.venue.exception.VenueAlreadyExistsException;
import com.eventhub.venue.exception.VenueCapacityTooLowException;
import com.eventhub.venue.exception.VenueIsBeingUsedException;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class VenueController
{
    private final VenueService venueService;

    public VenueController(VenueService venueService)
    {
        this.venueService = venueService;
    }

    @GetMapping("/venues")
    public ModelAndView getVenuesPage()
    {
        ModelAndView modelAndView = new ModelAndView("venues");
        modelAndView.addObject("venues", venueService.getAllVenues());
        return modelAndView;
    }

    @GetMapping("/venue-form")
    public ModelAndView getVenueFormPage()
    {
        VenueFormRequest venueForm = new VenueFormRequest();

        ModelAndView modelAndView = new ModelAndView("venue-form");
        modelAndView.addObject("venueForm", venueForm);
        return modelAndView;
    }

    @PostMapping("/venues")
    public ModelAndView createVenue(@Valid @ModelAttribute("venueForm") VenueFormRequest venueForm,
                                    BindingResult bindingResult, RedirectAttributes redirectAttributes)
    {
        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("venue-form");
            modelAndView.addObject("venueForm", venueForm);
            return modelAndView;
        }

        try
        {
            venueService.createVenue(venueForm);
        }
        catch(VenueAlreadyExistsException ex)
        {
            bindingResult.rejectValue("name", "venue.name.duplicate", ex.getMessage());

            ModelAndView modelAndView = new ModelAndView("venue-form");
            modelAndView.addObject("venueForm", venueForm);
            return modelAndView;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Venue created successfully.");

        return new ModelAndView("redirect:/venues");
    }

    @GetMapping("/venues/{id}/edit")
    public ModelAndView getVenueEditPage(@PathVariable UUID id)
    {
        Venue venue = venueService.getVenueById(id);

        VenueFormRequest venueForm = new VenueFormRequest();
        venueForm.setName(venue.getName());
        venueForm.setCity(venue.getCity());
        venueForm.setAddress(venue.getAddress());
        venueForm.setCapacity(venue.getCapacity());

        ModelAndView modelAndView = new ModelAndView("venue-form");
        modelAndView.addObject("venueForm", venueForm);
        modelAndView.addObject("venueId", id);
        return modelAndView;
    }

    @PutMapping("/venues/{id}")
    public ModelAndView updateVenue(@PathVariable UUID id,
                                    @Valid @ModelAttribute("venueForm") VenueFormRequest venueForm,
                                    BindingResult bindingResult)
    {
        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("venue-form");
            modelAndView.addObject("venueForm", venueForm);
            modelAndView.addObject("venueId", id);
            return modelAndView;
        }

        try
        {
            venueService.updateVenue(id, venueForm);
        }
        catch(VenueAlreadyExistsException ex)
        {
            bindingResult.rejectValue("name", "venue.name.duplicate", ex.getMessage());

            ModelAndView modelAndView = new ModelAndView("venue-form");
            modelAndView.addObject("venueForm", venueForm);
            modelAndView.addObject("venueId", id);
            return modelAndView;
        }
        catch(VenueCapacityTooLowException ex)
        {
            bindingResult.rejectValue("capacity", "venue.capacity.tooLow", ex.getMessage());

            ModelAndView modelAndView = new ModelAndView("venue-form");
            modelAndView.addObject("venueForm", venueForm);
            modelAndView.addObject("venueId", id);
            return modelAndView;
        }

        return new ModelAndView("redirect:/venues");
    }

    @DeleteMapping("/venues/{id}")
    public ModelAndView deleteVenue(@PathVariable UUID id)
    {
        try
        {
            venueService.deleteVenue(id);
            return new ModelAndView("redirect:/venues");
        }
        catch(VenueIsBeingUsedException ex)
        {
            ModelAndView modelAndView = new ModelAndView("venues");
            modelAndView.addObject("venues", venueService.getAllVenues());
            modelAndView.addObject("errorMessage", ex.getMessage());
            return modelAndView;
        }
    }
}
