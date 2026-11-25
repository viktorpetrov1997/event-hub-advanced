package com.eventhub.auth;

import com.eventhub.user.UserService;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.dto.UserLoginRequest;
import com.eventhub.user.dto.UserRegisterRequest;
import com.eventhub.user.exception.InvalidLoginException;
import com.eventhub.user.exception.UserAlreadyExistsException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ModelAndView getRegisterPage()
    {
        UserRegisterRequest userRegisterRequest = new UserRegisterRequest();

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("userRegisterRequest", userRegisterRequest);

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid UserRegisterRequest userRegisterRequest,
                                     BindingResult bindingResult)
    {
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
    public ModelAndView getLoginPage()
    {
        UserLoginRequest userLoginRequest = new UserLoginRequest();

        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("userLoginRequest", userLoginRequest);

        return modelAndView;
    }

    @PostMapping("/login")
    public ModelAndView login(@Valid UserLoginRequest userLoginRequest,
                              BindingResult bindingResult,
                              HttpSession httpSession
    )
    {
        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("login");
            modelAndView.addObject("userLoginRequest", userLoginRequest);
            return modelAndView;
        }

        try
        {
            UserDto user = userService.login(userLoginRequest);

            httpSession.setAttribute("user_id", user.getId());
            httpSession.setAttribute("user_role", user.getRole().name());
            httpSession.setAttribute("username", user.getUsername());

            return new ModelAndView("redirect:/");
        }
        catch(InvalidLoginException ex)
        {
            ModelAndView modelAndView = new ModelAndView("login");
            modelAndView.addObject("userLoginRequest", userLoginRequest);
            modelAndView.addObject("errorMessage", ex.getMessage());
            return modelAndView;
        }
    }

    @PostMapping("/logout")
    public ModelAndView logout(HttpSession httpSession)
    {
        httpSession.invalidate();
        return new ModelAndView("redirect:/");
    }
}
