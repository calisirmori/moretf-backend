package com.moretf.service;

import com.moretf.dto.UserProfileDTO;
import com.moretf.repository.activity.ActivityRepository;
import com.moretf.repository.classstats.ClassStatsRepository;
import com.moretf.repository.mapstats.MapStatsRepository;
import com.moretf.repository.peers.PeerRepository;
import com.moretf.repository.players.PlayerInfoRepository;
import com.moretf.repository.recentmatch.LogListRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProfileService {

    private final MapStatsRepository mapStatsRepository;
    private final PlayerInfoRepository playerInfoRepository;
    private final ClassStatsRepository classStatsRepository;
    private final LogListRepository logListRepository;
    private final ActivityRepository activityRepository;
    private final PeerRepository peerRepository;  // ✅ NEW

    public ProfileService(
            MapStatsRepository mapStatsRepository,
            PlayerInfoRepository playerInfoRepository,
            ClassStatsRepository classStatsRepository,
            LogListRepository logListRepository,
            ActivityRepository activityRepository,
            PeerRepository peerRepository // ✅ NEW
    ) {
        this.mapStatsRepository = mapStatsRepository;
        this.playerInfoRepository = playerInfoRepository;
        this.classStatsRepository = classStatsRepository;
        this.logListRepository = logListRepository;
        this.activityRepository = activityRepository;
        this.peerRepository = peerRepository;
    }

    public UserProfileDTO buildProfile(String id64) {
        UserProfileDTO dto = new UserProfileDTO();

        dto.mapStats = mapStatsRepository.findByUserId(id64);
        dto.classStats = classStatsRepository.findTopByUserId(id64);
        dto.recentMatches = logListRepository.findByUserId(id64);
        dto.activity = activityRepository.findRecentActivity(id64);

        var profileAndStats = playerInfoRepository.getProfileAndStats(id64);
        dto.profile = profileAndStats.profile;
        dto.overallStats = profileAndStats.overallStats;

        // ✅ Add top peers and enemies
        Map<String, List<UserProfileDTO.PeerEntry>> peerData = peerRepository.findTopPeersAndEnemies(id64);
        dto.topPeers = peerData.getOrDefault("topPeers", List.of());
        dto.topEnemies = peerData.getOrDefault("topEnemies", List.of());

        return dto;
    }
}
