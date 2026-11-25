package com.eventhub.event.exception;

public class EventHasActiveBookingsException extends IllegalStateException
{
    public EventHasActiveBookingsException(String message)
    {
        super(message);
    }
}
