package com.eventhub.user.exception;

public class InvalidLoginException extends IllegalArgumentException
{
    public InvalidLoginException(String message)
    {
        super(message);
    }
}
