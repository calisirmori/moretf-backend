package com.moretf.repository.players;

import com.moretf.dto.UserProfileDTO;

public interface PlayerInfoRepository {
    UserProfileDTO.ProfileAndStats getProfileAndStats(String id64);
}
