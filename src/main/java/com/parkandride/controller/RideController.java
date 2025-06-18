package com.parkandride.controller;

import com.parkandride.model.User;
import com.parkandride.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional; // Import Optional

@Controller
@RequestMapping("/rides")
public class RideController {

    private final UserService userService;

    @Autowired
    public RideController(UserService userService) {
        this.userService = userService;
    }

    private void addUserToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username); // Assign to Optional<User>
            if (userOptional.isPresent()) { // Check if user is present
                model.addAttribute("user", userOptional.get()); // Get user and add to model
            }
        }
    }

    @GetMapping("/last-mile")
    public String lastMileRides(Model model) {
        addUserToModel(model); // Add this line
        // Add any other needed model attributes
        return "rides/last-mile";
    }
    
    @GetMapping("/my-rides")
    public String myRides(Model model) {
        addUserToModel(model); // Add this line also for consistency
        // This will just render the template, as the ride data is stored in localStorage
        return "rides/my-rides";
    }
}