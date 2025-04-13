package com.main.server.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.main.server.model.Player;
import com.main.server.service.AccountService;
import com.main.server.service.DatabaseService;


/**
 * REST controller for Riot-related endpoints.
 * Routes API requests and delegates logic to AccountService.
 */
@RestController
@RequestMapping("/user")
public class Controller {

  private final DatabaseService databaseService;
  private final AccountService accountService;

  public Controller(DatabaseService databaseService, AccountService accountService) {
    this.databaseService = databaseService;
    this.accountService = accountService;
  }


  /**
   * GET /user/{riotId}/{tagLine}
   * 
   * Fetches a user's Riot account info based on Riot ID and tagline.
   * Example: /user/TheBestMid/NA1
   *
   * @param riotId the user's Riot game name (e.g., TheBestMid)
   * @param tagline the Riot tagLine (e.g., NA1)
   * @return UserType object or error message
   */
  @GetMapping("/{riotId}/{tagLine}")
  public ResponseEntity<?> getUser(@PathVariable String riotId, @PathVariable String tagLine) {
    try {
      Player user = accountService.getUserById(riotId, tagLine);
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/db/{puuid}")
  public ResponseEntity<?> getUserFromDb(@PathVariable String puuid) {
    try {
      Player player = databaseService.findByPuuid(puuid);
      return ResponseEntity.ok(player);
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("error", "Player not found"));
    }
  }

  @PutMapping("/upsert")
  public ResponseEntity<?> upsertUser(@RequestBody Player user){
    System.out.println("Start upsert");
    try {
      // Check if a user with the given puuid already exists
      Player existingUser = databaseService.findByPuuid(user.getPuuid());
      if (existingUser == null) {
        // If not, create a new record
        databaseService.saveUser(user);
      } else {
        // Otherwise, update the existing record with new gameName and tagline
        databaseService.updateUser(user);
      }
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
}
