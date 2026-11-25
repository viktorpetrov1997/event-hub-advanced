package com.eventhub.event.exception;

public class EventAlreadyStartedException extends IllegalStateException
{
    public EventAlreadyStartedException(String message)
    {
        super(message);
    }
}
