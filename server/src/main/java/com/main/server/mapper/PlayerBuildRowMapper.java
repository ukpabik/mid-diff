package com.main.server.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.main.server.model.PlayerBuild;

public class PlayerBuildRowMapper implements RowMapper<PlayerBuild>{

  @Override
  public PlayerBuild mapRow(ResultSet rs, int rowNum) throws SQLException {
    PlayerBuild pb = new PlayerBuild();

    // identity columns
    pb.setMatchId(rs.getString("match_id"));
    pb.setPuuid   (rs.getString("puuid"));

    // item slots
    pb.setItem0(rs.getInt("item0"));
    pb.setItem1(rs.getInt("item1"));
    pb.setItem2(rs.getInt("item2"));
    pb.setItem3(rs.getInt("item3"));
    pb.setItem4(rs.getInt("item4"));
    pb.setItem5(rs.getInt("item5"));
    pb.setItem6(rs.getInt("item6"));

    return pb;
  }
}
