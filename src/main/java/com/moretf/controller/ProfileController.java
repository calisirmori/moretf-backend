package com.moretf.controller;

import com.moretf.dto.UserProfileDTO;
import com.moretf.service.ProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{id64}")
    public UserProfileDTO getUserProfile(@PathVariable("id64") String id64) {
        return profileService.buildProfile(id64);
    }
}
