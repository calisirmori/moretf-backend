package com.moretf.repository.peers;

import com.moretf.dto.UserProfileDTO.PeerEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PeerRepositoryImpl implements PeerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, List<PeerEntry>> findTopPeersAndEnemies(String id64) {
        String sql = "SELECT * FROM get_top_peers_and_enemies(:id64)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);

        List<Object[]> rows = query.getResultList();

        List<PeerEntry> topPeers = new ArrayList<>();
        List<PeerEntry> topEnemies = new ArrayList<>();

        for (Object[] row : rows) {
            String type = (String) row[0]; // 'peer' or 'enemy'

            PeerEntry entry = new PeerEntry();
            entry.peerId64 = (String) row[1];
            entry.count = ((Number) row[2]).intValue();
            entry.wins = ((Number) row[3]).intValue();
            entry.loss = ((Number) row[4]).intValue();
            entry.ties = ((Number) row[5]).intValue();

            if ("peer".equals(type)) {
                topPeers.add(entry);
            } else if ("enemy".equals(type)) {
                topEnemies.add(entry);
            }
        }

        Map<String, List<PeerEntry>> result = new HashMap<>();
        result.put("topPeers", topPeers);
        result.put("topEnemies", topEnemies);
        return result;
    }
}
