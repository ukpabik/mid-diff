package com.main.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.main.server.model.Player;
import com.main.server.repository.PlayerRepository;

import jakarta.annotation.PostConstruct;

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

  public int saveUser(Player player){
    return playerRepository.save(player);
  }

  public Player findByPuuid(String puuid) {
    return playerRepository.findByPuuid(puuid);
  }
  public int updateUser(Player player) {
    return playerRepository.updatePlayer(player);
  }
}
