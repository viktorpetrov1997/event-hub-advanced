package com.eventhub.user.exception;

public class RoleChangeNotAllowedException extends IllegalStateException
{
    public RoleChangeNotAllowedException(String message)
    {
        super(message);
    }
}
