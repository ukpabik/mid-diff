package com.main.server.repository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.main.server.mapper.RankInfoRowMapper;
import com.main.server.model.RankInfo;


@Repository
public class RankRepository {
  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  /**
   * Saves a RankInfo record to the database.
   * In this example, we assume that a player can have one record per queue type.
   *
   * @param rankInfo the RankInfo object to save.
   * @return the number of rows affected.
   */
  public int save(RankInfo rankInfo) {
    String sql = """
      INSERT INTO rank_info (
        puuid, queue_type, tier, player_rank, league_points, wins, losses
      ) VALUES (?, ?, ?, ?, ?, ?, ?)
      ON CONFLICT (puuid, queue_type) DO UPDATE SET 
        tier = EXCLUDED.tier,
        player_rank = EXCLUDED.player_rank,
        league_points = EXCLUDED.league_points,
        wins = EXCLUDED.wins,
        losses = EXCLUDED.losses
    """;

    return jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql);
      ps.setString(1, rankInfo.getPuuid());
      ps.setString(2, rankInfo.getQueueType());
      ps.setString(3, rankInfo.getTier());
      ps.setString(4, rankInfo.getRank());
      ps.setInt(5, rankInfo.getLeaguePoints());
      ps.setInt(6, rankInfo.getWins());
      ps.setInt(7, rankInfo.getLosses());
      return ps;
    });
  }
  
  /**
   * Retrieves all RankInfo records for a given player (by puuid).
   *
   * @param puuid the player's unique identifier.
   * @return a list of RankInfo objects.
   */
  public List<RankInfo> findByPuuid(String puuid) {
    String sql = "SELECT * FROM rank_info WHERE puuid = ? ORDER BY queue_type";
    try {
      return jdbcTemplate.query(sql, new RankInfoRowMapper(), puuid);
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }
}
