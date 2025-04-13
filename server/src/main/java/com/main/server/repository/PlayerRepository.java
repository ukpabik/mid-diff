package com.main.server.repository;

import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.main.server.mapper.PlayerRowMapper;
import com.main.server.model.Player;

@Repository
public class PlayerRepository {
  @Autowired
  private JdbcTemplate jdbcTemplate;
  

  // TODO: This should handle errors
  public int save(Player player) {
    return jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO players (puuid, game_name, tag_line) VALUES (?, ?, ?)"
      );
      ps.setString(1, player.getPuuid());
      ps.setString(2, player.getGameName());
      ps.setString(3, player.getTagLine());
      return ps;
    });
  }
  public int updatePlayer(Player player) {
    String sql = "UPDATE players SET game_name = ?, tag_line = ? WHERE puuid = ?";
    return jdbcTemplate.update(sql,
      player.getGameName(),
      player.getTagLine(),
      player.getPuuid());
  }

  public Player findByPuuid(String puuid){
    String sql = "SELECT * FROM players WHERE puuid = ?";
    try {
      return jdbcTemplate.queryForObject(sql, new PlayerRowMapper(), puuid);
    } catch (Exception e) {
      return null;
    }
  }


  public List<Player> findAll(){
    String sql = "SELECT * FROM players";
    return jdbcTemplate.query(sql, new PlayerRowMapper());
  }



  // Function to test database connection
  public boolean testConnection() {
    try {
      jdbcTemplate.execute("SELECT 1");
      System.out.println("✅ Supabase connection is working.");
      return true;
    } catch (Exception e) {
      System.err.println("❌ Supabase connection failed: " + e.getMessage());
      return false;
    }
  }

}
