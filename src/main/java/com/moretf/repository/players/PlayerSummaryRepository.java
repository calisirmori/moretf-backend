package com.moretf.repository.players;

import com.moretf.model.PlayerSummaryEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlayerSummaryRepository {
    @Transactional
    void bulkInsert(List<PlayerSummaryEntity> players);
}
