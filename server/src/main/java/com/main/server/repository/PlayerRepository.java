package com.main.server.repository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.main.server.mapper.PlayerRowMapper;
import com.main.server.model.Player;

/**
 * Repository class for performing database operations on the `players` table.
 * Provides CRUD functionality using Spring's JdbcTemplate.
 */
@Repository
public class PlayerRepository {
  @Autowired
  private JdbcTemplate jdbcTemplate;
  

  public int save(Player player) {
    return jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO players (puuid, game_name, tag_line, profile_picture) VALUES (?, ?, ?, ?)"
      );
      ps.setString(1, player.getPuuid());
      ps.setString(2, player.getGameName());
      ps.setString(3, player.getTagLine());
      String profileIconId = player.getProfileIconId(); 
      ps.setString(4, profileIconId);
      return ps;
    });
  }
  public int updatePlayer(Player player) {
    String sql = "UPDATE players SET game_name = ?, tag_line = ?, profile_picture = ? WHERE puuid = ?";
    return jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(sql);
      ps.setString(1, player.getGameName());
      ps.setString(2, player.getTagLine());
      ps.setString(3, player.getProfileIconId());
      ps.setString(4, player.getPuuid());
      return ps;
    });
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
    try{
      return jdbcTemplate.query(sql, new PlayerRowMapper());
    }
    catch(Exception e){
      return new ArrayList<>();
    }
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
