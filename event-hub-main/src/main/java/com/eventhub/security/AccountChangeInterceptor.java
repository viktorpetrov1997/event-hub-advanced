package com.eventhub.security;

import com.eventhub.user.UserService;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AccountChangeInterceptor implements HandlerInterceptor
{
    private final UserService userService;

    public AccountChangeInterceptor(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser))
        {
            return true;
        }

        try
        {
            UserDto currentUser = userService.getById(authenticatedUser.getId());

            if(currentUser.getRole() == authenticatedUser.getRole())
            {
                return true;
            }

            return logOut(request, response, "/login?expired");
        }
        catch(UserNotFoundException ex)
        {
            return logOut(request, response, "/login?deleted");
        }
    }

    private boolean logOut(HttpServletRequest request, HttpServletResponse response, String redirectUrl) throws IOException
    {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        response.sendRedirect(redirectUrl);

        return false;
    }
}
