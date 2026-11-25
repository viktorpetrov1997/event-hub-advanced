package com.eventhub.event.exception;

public class EventAlreadyEndedException extends IllegalStateException
{
    public EventAlreadyEndedException(String message)
    {
        super(message);
    }
}
