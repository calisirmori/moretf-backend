package com.moretf.repository;

import com.moretf.model.PlayerSummaryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlayerSummaryRepositoryImpl implements PlayerSummaryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void bulkInsert(List<PlayerSummaryEntity> players) {
        String sql = "INSERT INTO players (kills, assists, deaths, dpm, damage, dtm, dt, heals, player_class, team, player_name, match_result, id64, logid, time_played) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, players, 100, (ps, p) -> {
            ps.setInt(1, p.getKills());
            ps.setInt(2, p.getAssists());
            ps.setInt(3, p.getDeaths());
            ps.setInt(4, p.getDpm());
            ps.setInt(5, p.getDamage());
            ps.setInt(6, p.getDtm());
            ps.setInt(7, p.getDt());
            ps.setInt(8, p.getHeals());
            ps.setString(9, p.getPlayerClass());
            ps.setString(10, p.getTeam());
            ps.setString(11, p.getPlayerName());
            ps.setString(12, p.getMatchResult());
            ps.setLong(13, p.getId64());
            ps.setLong(14, p.getLogid());
            ps.setInt(15, p.getTimePlayed());
        });
    }
}