package com.eventhub.venue.exception;

public class VenueCapacityTooLowException extends IllegalArgumentException
{
    public VenueCapacityTooLowException(String message)
    {
        super(message);
    }
}
