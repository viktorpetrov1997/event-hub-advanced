package com.eventhub.event.exception;

public class EventCapacityBelowBookedSeatsException extends IllegalArgumentException
{
    public EventCapacityBelowBookedSeatsException(String message)
    {
        super(message);
    }
}
