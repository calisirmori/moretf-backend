package com.moretf.controller;

import com.moretf.dto.UserProfileDTO.DailyActivity;
import com.moretf.repository.activity.ActivityRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class PlayerActivityController {

    private final ActivityRepository activityRepository;

    public PlayerActivityController(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @GetMapping
    public List<DailyActivity> getAllActivity(@RequestParam String id64) {
        return activityRepository.findAllActivity(id64);
    }
}
