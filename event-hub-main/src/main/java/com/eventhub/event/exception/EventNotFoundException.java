package com.eventhub.event.exception;

public class EventNotFoundException extends RuntimeException
{
    public EventNotFoundException(String message)
    {
        super(message);
    }
}
