package com.main.server.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.main.server.mapper.PlayerBuildRowMapper;
import com.main.server.model.PlayerBuild;

@Repository
public class PlayerBuildRepository {
  private static final Logger logger = LoggerFactory.getLogger(PlayerBuildRepository.class);
  @Autowired
  private JdbcTemplate jdbcTemplate;

  public int save(PlayerBuild build) {
    String sql = """
        INSERT INTO player_builds (
          match_id, puuid,
          item0, item1, item2, item3,
          item4, item5, item6
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (match_id, puuid) DO NOTHING
        """;

    try {
      return jdbcTemplate.update(sql,
        build.getMatchId(),
        build.getPuuid(),
        build.getItem0(),
        build.getItem1(),
        build.getItem2(),
        build.getItem3(),
        build.getItem4(),
        build.getItem5(),
        build.getItem6()
      );
    } catch (DataAccessException dae) {
      logger.error(
        "Failed to save PlayerBuild (match_id={}, puuid={}): {}",
        build.getMatchId(),
        build.getPuuid(),
        dae.getRootCause() != null
            ? dae.getRootCause().getMessage()
            : dae.getMessage(),
        dae
      );
      throw dae;
    }
  }

  public PlayerBuild findByMatchIdAndPuuid(String matchId, String puuid) {
    String sql = "SELECT * FROM player_builds WHERE match_id = ? AND puuid = ?";
    try {
      return jdbcTemplate.queryForObject(
        sql,
        new PlayerBuildRowMapper(),
        new Object[]{matchId, puuid}            
      );
    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
      return null;
    }
  }

  public Set<String> findMatchIdsByPuuid(String puuid) {
    String sql = """
      SELECT match_id
        FROM player_builds
        WHERE puuid = ?
      """;
    List<String> ids = jdbcTemplate.queryForList(
      sql,
      String.class,
      puuid
    );
    return new HashSet<>(ids);
  }
}
