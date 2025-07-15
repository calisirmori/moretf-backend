package com.moretf.repository;

import com.moretf.dto.UserProfileDTO;

public interface PlayerInfoRepository {
    UserProfileDTO.ProfileAndStats getProfileAndStats(String id64);
}
