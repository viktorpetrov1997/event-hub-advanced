package com.eventhub.venue.exception;

public class VenueAlreadyExistsException extends IllegalArgumentException
{
    public VenueAlreadyExistsException(String message)
    {
        super(message);
    }
}
