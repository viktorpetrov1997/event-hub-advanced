package com.eventhub.venue;

import com.eventhub.event.EventRepository;
import com.eventhub.venue.dto.VenueFormRequest;
import com.eventhub.venue.exception.VenueAlreadyExistsException;
import com.eventhub.venue.exception.VenueCapacityTooLowException;
import com.eventhub.venue.exception.VenueIsBeingUsedException;
import com.eventhub.venue.exception.VenueNotFoundException;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VenueService
{
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public VenueService(VenueRepository venueRepository, EventRepository eventRepository)
    {
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    public List<Venue> getAllVenues()
    {
        return venueRepository.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    public void createVenue(VenueFormRequest request)
    {
        if(venueRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndActiveTrue(request.getName(), request.getCity()))
        {
            throw new VenueAlreadyExistsException("A venue with this name already exists in this city.");
        }

        Venue venue = Venue.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .capacity(request.getCapacity())
                .build();

        venueRepository.save(venue);
    }

    public Venue getVenueById(UUID id)
    {
        return venueRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
    }

    public void updateVenue(UUID id, VenueFormRequest request)
    {
        Venue venue = getVenueById(id);

        if(venueRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndActiveTrueAndIdNot(request.getName(), request.getCity(), id))
        {
            throw new VenueAlreadyExistsException("A venue with this name already exists in this city.");
        }

        int requiredCapacity = eventRepository.findMaxUpcomingEventCapacityAtVenue(id, LocalDateTime.now());

        if(request.getCapacity() < requiredCapacity)
        {
            throw new VenueCapacityTooLowException(
                    "Capacity cannot be lower than " + requiredCapacity
                            + " because an upcoming or ongoing event at this venue has that many seats.");
        }

        venue.setName(request.getName());
        venue.setCity(request.getCity());
        venue.setAddress(request.getAddress());
        venue.setCapacity(request.getCapacity());

        venueRepository.save(venue);
    }

    public void deleteVenue(UUID id)
    {
        Venue venue = getVenueById(id);

        if(eventRepository.existsByVenueIdAndEndTimeAfter(id, LocalDateTime.now()))
        {
            throw new VenueIsBeingUsedException("This venue cannot be deleted because it has upcoming or ongoing events.");
        }

        if(eventRepository.existsByVenueId(id))
        {
            venue.setActive(false);
            venueRepository.save(venue);
            return;
        }

        venueRepository.delete(venue);
    }
}
