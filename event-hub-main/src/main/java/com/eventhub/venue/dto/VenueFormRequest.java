package com.eventhub.venue.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VenueFormRequest
{
    @NotBlank(message = "Venue name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "City is required")
    @Size(min = 2, max = 60, message = "City must be between 2 and 60 characters")
    private String city;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 150, message = "Address must be between 5 and 150 characters")
    private String address;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 100000, message = "Capacity cannot exceed 100,000")
    private Integer capacity;
}
