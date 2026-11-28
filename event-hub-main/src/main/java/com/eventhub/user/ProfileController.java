package com.eventhub.user;

import com.eventhub.security.AuthenticatedUser;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.dto.UserProfileEditRequest;
import com.eventhub.user.exception.UserAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProfileController
{
    private final UserService userService;

    public ProfileController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("user", userService.getById(authenticatedUser.getId()));
        return modelAndView;
    }

    @GetMapping("/profile/edit")
    public ModelAndView getProfileEditPage(@AuthenticationPrincipal AuthenticatedUser authenticatedUser)
    {
        UserDto user = userService.getById(authenticatedUser.getId());

        UserProfileEditRequest profileForm = new UserProfileEditRequest();
        profileForm.setUsername(user.getUsername());
        profileForm.setEmail(user.getEmail());

        ModelAndView modelAndView = new ModelAndView("profile-edit");
        modelAndView.addObject("profileForm", profileForm);
        return modelAndView;
    }

    @PutMapping("/profile")
    public ModelAndView updateProfile(@Valid @ModelAttribute("profileForm") UserProfileEditRequest profileForm,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                      HttpServletRequest request,
                                      HttpServletResponse response)
    {
        if(bindingResult.hasErrors())
        {
            ModelAndView modelAndView = new ModelAndView("profile-edit");
            modelAndView.addObject("profileForm", profileForm);
            return modelAndView;
        }

        try
        {
            userService.updateProfile(authenticatedUser.getId(), profileForm);
        }
        catch(UserAlreadyExistsException ex)
        {
            ModelAndView modelAndView = new ModelAndView("profile-edit");
            modelAndView.addObject("profileForm", profileForm);
            modelAndView.addObject("errorMessage", ex.getMessage());
            return modelAndView;
        }

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        return new ModelAndView("redirect:/login?profileUpdated");
    }
}
