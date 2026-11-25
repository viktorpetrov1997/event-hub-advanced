package com.eventhub.venue.exception;

public class VenueIsBeingUsedException extends IllegalStateException
{
    public VenueIsBeingUsedException(String message)
    {
        super(message);
    }
}
