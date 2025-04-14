package com.main.server.repository;

import com.main.server.mapper.MatchRowMapper;
import com.main.server.model.Match;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository for handling all database operations related to matches.
 * Supports inserting, checking, and querying match data cached from Riot API.
 */
@Repository
public class MatchRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public int save(Match m) {
    String sql = """
      insert into matches (
        match_id, puuid, champion_name, team_position, win,
        kills, deaths, assists, gold_earned, gold_spent,
        total_minions_killed, neutral_minions_killed,
        damage_dealt_to_champions, total_damage_taken,
        vision_score, wards_placed, wards_killed,
        turret_takedowns, inhibitor_takedowns,
        game_start_timestamp, game_duration, game_mode, queue_id,
        cs_per_min, kda
      ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      on conflict (match_id) do nothing
    """;

    return jdbcTemplate.update(sql,
      m.getMatchId(), m.getPuuid(), m.getChampionName(), m.getTeamPosition(), m.isWin(),
      m.getKills(), m.getDeaths(), m.getAssists(),
      m.getGoldEarned(), m.getGoldSpent(),
      m.getTotalMinionsKilled(), m.getNeutralMinionsKilled(),
      m.getDamageDealtToChampions(), m.getTotalDamageTaken(),
      m.getVisionScore(), m.getWardsPlaced(), m.getWardsKilled(),
      m.getTurretTakedowns(), m.getInhibitorTakedowns(),
      m.getGameStartTimestamp(), m.getGameDuration(), m.getGameMode(), m.getQueueId(),
      m.getCsPerMin(), m.getKda()
  );
  }

  public Set<String> findExistingMatchIds(List<String> ids) {
    String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
    String sql = "SELECT match_id FROM matches WHERE match_id IN (" + inSql + ")";
    List<String> existing = jdbcTemplate.queryForList(sql, String.class, ids.toArray());
    return new HashSet<>(existing);
  }

  public List<Match> findByPuuid(String puuid) {
    String sql = "SELECT * FROM matches WHERE puuid = ? ORDER BY game_start_timestamp DESC";
    return jdbcTemplate.query(sql, new MatchRowMapper(), puuid);
  }

  public List<Match> findAll() {
    String sql = "SELECT * FROM matches ORDER BY game_start_timestamp DESC";
    return jdbcTemplate.query(sql, new MatchRowMapper());
  }
  
}
