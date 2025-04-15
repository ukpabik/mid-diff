package com.main.server.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.main.server.model.Player;


public class PlayerRowMapper implements RowMapper<Player>{
  @Override
  public Player mapRow(ResultSet rs, int rowNum) throws SQLException{
    Player player = new Player();
    player.setGameName(rs.getString("game_name"));
    player.setPuuid(rs.getString("puuid"));
    player.setTagLine(rs.getString("tag_line"));
    player.setProfileIconId(rs.getString("profile_picture"));

    return player;
  }
}
