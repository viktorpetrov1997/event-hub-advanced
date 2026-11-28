package com.eventhub.auth;

import com.eventhub.security.AuthenticatedUser;
import com.eventhub.user.UserService;
import com.eventhub.user.dto.UserRegisterRequest;
import com.eventhub.user.exception.UserAlreadyExistsException;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AuthController
{
    private final UserService userService;

    public AuthController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        if(authenticatedUser != null)
        {
            return new ModelAndView("redirect:/events");
        }

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("userRegisterRequest", userRegisterRequest);

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid UserRegisterRequest userRegisterRequest,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        if(authenticatedUser != null)
        {
            return new ModelAndView("redirect:/events");
        }

        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("register");
            modelAndView.addObject("userRegisterRequest", userRegisterRequest);
            return modelAndView;
        }

        try
        {
            userService.register(userRegisterRequest);
        }
        catch(UserAlreadyExistsException ex)
        {
            ModelAndView modelAndView = new ModelAndView("register");
            modelAndView.addObject("userRegisterRequest", userRegisterRequest);
            modelAndView.addObject("errorMessage", ex.getMessage());
            return modelAndView;
        }

        return new ModelAndView("redirect:/login");
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(required = false) String error,
                                     @RequestParam(required = false) String expired,
                                     @RequestParam(required = false) String deleted,
                                     @AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        if(authenticatedUser != null)
        {
            return new ModelAndView("redirect:/events");
        }

        ModelAndView modelAndView = new ModelAndView("login");

        if(error != null)
        {
            modelAndView.addObject("errorMessage", "Invalid email or password!");
        }

        if(expired != null)
        {
            modelAndView.addObject("errorMessage", "Your account was changed by an administrator. Please log in again.");
        }

        if(deleted != null)
        {
            modelAndView.addObject("errorMessage", "Your account no longer exists.");
        }

        return modelAndView;
    }
}
