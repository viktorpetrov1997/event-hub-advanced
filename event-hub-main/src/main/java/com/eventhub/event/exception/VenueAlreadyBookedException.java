package com.eventhub.event.exception;

public class VenueAlreadyBookedException extends IllegalArgumentException
{
    public VenueAlreadyBookedException(String message)
    {
        super(message);
    }
}
