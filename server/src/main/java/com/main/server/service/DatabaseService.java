package com.main.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.main.server.model.Player;
import com.main.server.repository.PlayerRepository;

import jakarta.annotation.PostConstruct;


/**
 * Service layer responsible for interacting with the database layer
 * to manage {@link Player} entities.
 *
 */
@Service
public class DatabaseService {
  @Autowired
  private PlayerRepository playerRepository;

  // Test to see if we connect to database (will remove later)
  @PostConstruct
  public void checkConnection() {
    System.out.println("🔎 Testing DB connection...");
    playerRepository.testConnection();
  }

  /**
   * Saves a new player to the database.
   *
   * @param player the player object to persist
   * @return number of rows affected (should be 1 on success)
   */
  public int saveUser(Player player){
    return playerRepository.save(player);
  }

  /**
   * Finds a player in the database by their Riot PUUID.
   *
   * @param puuid the player's PUUID
   * @return {@link Player} object if found; otherwise, may throw exception
   */
  public Player findByPuuid(String puuid) {
    return playerRepository.findByPuuid(puuid);
  }

  /**
   * Updates an existing player’s gameName and tagLine.
   *
   * @param player the player object with updated values
   * @return number of rows affected (should be 1 on success)
   */
  public int updateUser(Player player) {
    return playerRepository.updatePlayer(player);
  }
}
