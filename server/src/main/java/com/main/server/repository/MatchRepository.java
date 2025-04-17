package com.main.server.repository;

import com.main.server.mapper.MatchRowMapper;
import com.main.server.model.Match;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository for handling all database operations related to matches.
 * Supports inserting, checking, and querying match data cached from Riot API.
 */
@Repository
public class MatchRepository {
  private static final Logger logger = LoggerFactory.getLogger(MatchRepository.class);
  @Autowired
  private JdbcTemplate jdbcTemplate;

  public int save(Match m) {
    String sql = """
      INSERT INTO matches (
        match_id, puuid, champion_name, champion_id, team_position, win,
        kills, deaths, assists, gold_earned, gold_spent,
        total_minions_killed, neutral_minions_killed,
        damage_dealt_to_champions, total_damage_taken,
        vision_score, wards_placed, wards_killed,
        turret_takedowns, inhibitor_takedowns,
        game_start_timestamp, game_duration, game_mode, queue_id,
        cs_per_min, kda
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT (match_id, puuid) DO NOTHING
      """;

    try {
      return jdbcTemplate.update(sql,
        m.getMatchId(),
        m.getPuuid(),
        m.getChampionName(),
        m.getChampionId(),
        m.getTeamPosition(),
        m.isWin(),
        m.getKills(),
        m.getDeaths(),
        m.getAssists(),
        m.getGoldEarned(),
        m.getGoldSpent(),
        m.getTotalMinionsKilled(),
        m.getNeutralMinionsKilled(),
        m.getDamageDealtToChampions(),
        m.getTotalDamageTaken(),
        m.getVisionScore(),
        m.getWardsPlaced(),
        m.getWardsKilled(),
        m.getTurretTakedowns(),
        m.getInhibitorTakedowns(),
        m.getGameStartTimestamp(),
        m.getGameDuration(),
        m.getGameMode(),
        m.getQueueId(),
        m.getCsPerMin(),
        m.getKda()
      );
    } 
    catch (DataAccessException dae) {
      logger.error(
        "Failed to save match (match_id={}, puuid={}) to DB: {}",
        m.getMatchId(), m.getPuuid(), dae.getRootCause() != null ? dae.getRootCause().getMessage() : dae.getMessage(),
        dae
      );
      throw dae;
    }
  }
  

  public Set<String> findExistingMatchIdsForUser(List<String> ids, String puuid) {
    if (ids.isEmpty()) {
      return Collections.emptySet();
    }
    String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
  
    String sql = ""
      + "SELECT match_id "
      + "FROM matches "
      + "WHERE puuid = ? "
      + "  AND match_id IN (" + placeholders + ")";
  
    Object[] params = new Object[1 + ids.size()];
    params[0] = puuid;
    for (int i = 0; i < ids.size(); i++) {
      params[i + 1] = ids.get(i);
    }
  
    List<String> existing = jdbcTemplate.queryForList(sql, String.class, params);
  
    return new HashSet<>(existing);
  }
  

  /**
   * Pulls the last 20 matches from database.
   * @param puuid
   * @return A list of the most recent {@link Match} objects.
   */
  public List<Match> findByPuuid(String puuid) {
    String sql = "SELECT * FROM matches WHERE puuid = ? ORDER BY game_start_timestamp DESC LIMIT 20";
    return jdbcTemplate.query(sql, new MatchRowMapper(), puuid);
  }

  public List<Match> findAll() {
    String sql = "SELECT * FROM matches ORDER BY game_start_timestamp DESC";
    return jdbcTemplate.query(sql, new MatchRowMapper());
  }
  
  
}
