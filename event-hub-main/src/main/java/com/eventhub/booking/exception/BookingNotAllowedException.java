package com.eventhub.booking.exception;

public class BookingNotAllowedException extends IllegalStateException
{
    public BookingNotAllowedException(String message)
    {
        super(message);
    }
}
