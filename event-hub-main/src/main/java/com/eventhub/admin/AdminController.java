package com.eventhub.admin;

import com.eventhub.security.AuthenticatedUser;
import com.eventhub.user.UserRole;
import com.eventhub.user.UserService;
import com.eventhub.user.dto.UserDto;
import com.eventhub.user.exception.RoleChangeNotAllowedException;
import com.eventhub.user.exception.UserNotFoundException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class AdminController
{
    private final UserService userService;

    public AdminController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public ModelAndView getUsersPage()
    {
        ModelAndView modelAndView = new ModelAndView("admin-users");
        modelAndView.addObject("users", userService.getAllUsers());
        modelAndView.addObject("roles", UserRole.values());
        return modelAndView;
    }

    @PutMapping("/admin/users/{id}/role")
    public ModelAndView changeUserRole(@PathVariable UUID id,
                                       @RequestParam UserRole role,
                                       @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                       RedirectAttributes redirectAttributes)
    {
        try
        {
            UserDto user = userService.changeRole(id, role, authenticatedUser.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Role of " + user.getUsername() + " is now " + user.getRole() + ". They will need to log in again.");
        }
        catch(RoleChangeNotAllowedException | UserNotFoundException ex)
        {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return new ModelAndView("redirect:/admin/users");
    }
}
