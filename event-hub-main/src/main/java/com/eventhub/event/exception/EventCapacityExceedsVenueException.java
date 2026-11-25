package com.eventhub.event.exception;

public class EventCapacityExceedsVenueException extends IllegalArgumentException
{
    public EventCapacityExceedsVenueException(String message)
    {
        super(message);
    }
}
