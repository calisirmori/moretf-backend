package com.moretf.repository.peers;

import com.moretf.dto.UserProfileDTO.PeerEntry;
import java.util.List;
import java.util.Map;

public interface PeerRepository {
    Map<String, List<PeerEntry>> findTopPeersAndEnemies(String id64);
}
