package com.main.server.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.main.server.model.RankInfo;

public class RankInfoRowMapper implements RowMapper<RankInfo> {

  @Override
  public RankInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
    RankInfo rankInfo = new RankInfo();
    rankInfo.setPuuid(rs.getString("puuid"));
    rankInfo.setQueueType(rs.getString("queue_type"));
    rankInfo.setTier(rs.getString("tier"));
    rankInfo.setRank(rs.getString("player_rank"));
    rankInfo.setLeaguePoints(rs.getInt("league_points"));
    rankInfo.setWins(rs.getInt("wins"));
    rankInfo.setLosses(rs.getInt("losses"));
    return rankInfo;
  }
  
}
