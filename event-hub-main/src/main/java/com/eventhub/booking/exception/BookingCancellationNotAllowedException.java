package com.eventhub.booking.exception;

public class BookingCancellationNotAllowedException extends IllegalStateException
{
    public BookingCancellationNotAllowedException(String message)
    {
        super(message);
    }
}
