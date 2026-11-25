package com.eventhub.security;

import com.eventhub.user.UserRole;
import com.eventhub.user.UserService;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class SessionInterceptor implements HandlerInterceptor
{
    private static final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/", "/login", "/register", "/error");
    private static final Set<String> GUEST_ONLY_ENDPOINTS = Set.of("/login", "/register");

    private final UserService userService;

    public SessionInterceptor(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        String endpoint = request.getServletPath();

        HttpSession httpSession = request.getSession(false);

        UUID userId = httpSession == null ? null : (UUID) httpSession.getAttribute("user_id");

        if(UNAUTHENTICATED_ENDPOINTS.contains(endpoint))
        {
            if(userId != null && GUEST_ONLY_ENDPOINTS.contains(endpoint))
            {
                response.sendRedirect("/events");
                return false;
            }

            return true;
        }

        if(userId == null)
        {
            response.sendRedirect("/login");
            return false;
        }

        UserDto user;

        try
        {
            user = userService.getById(userId);
        }
        catch(UserNotFoundException ex)
        {
            httpSession.invalidate();
            response.sendRedirect("/login");
            return false;
        }

        boolean organizerOnly = endpoint.equals("/dashboard")
                || endpoint.equals("/event-form")
                || (endpoint.equals("/events") && request.getMethod().equals("POST"))
                || (endpoint.startsWith("/events/") && (request.getMethod().equals("PUT") || request.getMethod().equals("DELETE")))
                || endpoint.startsWith("/venue")
                || endpoint.endsWith("/edit");

        if(organizerOnly && user.getRole() != UserRole.ORGANIZER)
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("You do not have permission to access this resource.");
            return false;
        }

        boolean attendeeOnly = endpoint.equals("/my-bookings")
                || endpoint.endsWith("/book")
                || endpoint.endsWith("/cancel");

        if(attendeeOnly && user.getRole() != UserRole.ATTENDEE)
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("You do not have permission to access this resource.");
            return false;
        }

        return true;
    }
}
