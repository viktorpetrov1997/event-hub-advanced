package com.eventhub.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class BookingFormRequest
{
    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat is required")
    private Integer seats;
}