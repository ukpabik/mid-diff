package com.main.server.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import com.main.server.model.Match;

public class MatchRowMapper implements RowMapper<Match> {
  
  @Override
  public Match mapRow(ResultSet rs, int rowNum) throws SQLException {
    Match m = new Match();
    m.setMatchId(rs.getString("match_id"));
    m.setPuuid(rs.getString("puuid"));
    m.setChampionName(rs.getString("champion_name"));
    m.setChampionId(rs.getString("champion_id"));
    m.setRole(rs.getString("role"));
    m.setTeamPosition(rs.getString("team_position"));
    m.setWin(rs.getBoolean("win"));
    m.setKills(rs.getInt("kills"));
    m.setDeaths(rs.getInt("deaths"));
    m.setAssists(rs.getInt("assists"));
    m.setGoldEarned(rs.getInt("gold_earned"));
    m.setGoldSpent(rs.getInt("gold_spent"));
    m.setTotalMinionsKilled(rs.getInt("total_minions_killed"));
    m.setNeutralMinionsKilled(rs.getInt("neutral_minions_killed"));
    m.setDamageDealtToChampions(rs.getInt("damage_dealt_to_champions"));
    m.setTotalDamageTaken(rs.getInt("total_damage_taken"));
    m.setVisionScore(rs.getInt("vision_score"));
    m.setWardsPlaced(rs.getInt("wards_placed"));
    m.setWardsKilled(rs.getInt("wards_killed"));
    m.setTurretTakedowns(rs.getInt("turret_takedowns"));
    m.setInhibitorTakedowns(rs.getInt("inhibitor_takedowns"));
    m.setGameStartTimestamp(rs.getLong("game_start_timestamp"));
    m.setGameDuration(rs.getLong("game_duration"));
    m.setGameMode(rs.getString("game_mode"));
    m.setQueueId(rs.getInt("queue_id"));
    m.setCsPerMin(rs.getDouble("cs_per_min"));
    m.setKda(rs.getDouble("kda"));
    return m;
  }
}
