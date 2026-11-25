package com.eventhub.venue.exception;

public class VenueNotFoundException extends RuntimeException
{
    public VenueNotFoundException(String message)
    {
        super(message);
    }
}
