package com.eventhub.booking.exception;

public class NotEnoughSeatsException extends IllegalArgumentException
{
    public NotEnoughSeatsException(String message)
    {
        super(message);
    }
}
