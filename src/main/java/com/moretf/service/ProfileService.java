package com.moretf.service;

import com.moretf.dto.UserProfileDTO;
import com.moretf.repository.MapStatsRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final MapStatsRepository mapStatsRepository;

    public ProfileService(MapStatsRepository mapStatsRepository) {
        this.mapStatsRepository = mapStatsRepository;
    }

    public UserProfileDTO buildProfile(String id64) {
        UserProfileDTO dto = new UserProfileDTO();

        // Later you can fill in username, team, overall, etc
        dto.mapStats = mapStatsRepository.findByUserId(id64);

        return dto;
    }
}
