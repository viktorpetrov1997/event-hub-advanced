package com.eventhub.event.dto;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EventFormRequest
{
    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Event description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Event date and time are required")
    @Future(message = "Event date must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "End date and time are required")
    @Future(message = "Event end must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    @AssertTrue(message = "End time must be after the start time")
    public boolean isEndTimeAfterStartTime()
    {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price can have up to 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private boolean published;

    @NotNull(message = "Please select a venue")
    private UUID venueId;
}
