package com.main.server.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.main.server.model.Player;
import com.main.server.service.CsvService;
import com.main.server.service.DatabaseService;
import com.main.server.service.RiotService;


/**
 * REST controller for user-related endpoints involving Riot API integration and database access.
 * 
 * This controller exposes endpoints to:
 * - Fetch a Riot user's info via Riot ID and tagline
 * - Upsert users into the database
 * - Cache recent match data in the background
 * - Fetch cached match data
 */
@RestController
@RequestMapping("/user")
public class Controller {

  private final DatabaseService databaseService;
  private final RiotService accountService;
  private final CsvService csvService;

  public Controller(DatabaseService databaseService, RiotService accountService, CsvService csvService) {
    this.databaseService = databaseService;
    this.accountService = accountService;
    this.csvService = csvService;
  }

  /**
   * Fetches a Riot user by game name and tagline using the Riot API.
   *
   * @param riotId the Riot game name (e.g., "TheBestMid")
   * @param tagLine the tagline associated with the Riot ID (e.g., "NA1")
   * @return {@link Player} object containing user info or error message on failure
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

  /**
   * Retrieves a user from the Supabase database using their PUUID.
   *
   * @param puuid the PUUID of the player
   * @return {@link Player} object from the database or a 404 error if not found
   */
  @GetMapping("/db/{puuid}")
  public ResponseEntity<?> getUserFromDb(@PathVariable String puuid) {
    try {
      Player player = databaseService.findByPuuid(puuid);
      return ResponseEntity.ok(player);
    } catch (Exception e) {
      return ResponseEntity.status(404).body(Map.of("error", "Player not found"));
    }
  }

  /**
   * Upserts a user into the database. If the user exists, updates their name/tag; otherwise inserts a new user.
   *
   * @param user the {@link Player} object to be saved or updated
   * @return the upserted user or an error response
   */
  @PutMapping("/upsert")
  public ResponseEntity<?> upsertUser(@RequestBody Player user){
    try {
      // Check if a user with the given puuid already exists
      Player existingUser = databaseService.findByPuuid(user.getPuuid());
      if (existingUser == null) {
        databaseService.saveUser(user);
      } else {
        databaseService.updateUser(user);
      }
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Performs a full account + match sync:
   *  Fetches Riot account info via Riot API
   *  Upserts the user in the Supabase DB
   *  Triggers match caching for the 20 most recent ranked matches
   * 
   *
   * @param riotId the Riot game name
   * @param tagLine the Riot tagline
   * @return the upserted user or error message
   */
  @GetMapping("/search/{riotId}/{tagLine}")
  public ResponseEntity<?> searchAndCache(@PathVariable String riotId, @PathVariable String tagLine) {
    try {
      // Get user from Riot
      Player user = accountService.getUserById(riotId, tagLine);
      System.out.println(user);
      // Upsert into db
      Player existing = databaseService.findByPuuid(user.getPuuid());
      if (existing == null) {
        databaseService.saveUser(user);
      } else {
        databaseService.updateUser(user);
      }
      
      // Fetch 20 recent match IDs (blocking)
      List<String> ids = accountService.getRecentMatchIds(user.getPuuid(), "ranked", 20);

      // Synchronously cache them
      accountService.cacheMissingMatches(ids, user.getPuuid());

      // Return the user (or data) after all matches are saved
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }



  /**
   * Fetches cached match data for a given player PUUID from Supabase.
   * Intended for frontend polling after search and background cache are triggered.
   *
   * @param puuid the PUUID of the player
   * @return a list of cached match objects or error if lookup fails
   */
  @GetMapping("/matches/{puuid}")
  public ResponseEntity<?> getCachedMatches(@PathVariable String puuid) {
    try {
      return ResponseEntity.ok(accountService.getCachedMatches(puuid));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }


  @GetMapping("/debug/trigger-csv")
  public ResponseEntity<?> triggerCsvExport(){
    csvService.generateCsvAsync();
    return ResponseEntity.ok(Map.of("status", "CSV export started in background"));
  }

  @GetMapping("/debug/download-csv")
  public ResponseEntity<?> downloadCsv(){
    File csvFile = csvService.getLatestCsv();
    if (csvFile == null || !csvFile.exists()) {
      return ResponseEntity.status(404).body(Map.of("error", "No CSV has been generated yet"));
    }

    try {
      InputStreamResource resource = new InputStreamResource(new FileInputStream(csvFile));
      return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=match_export.csv")
        .header("Content-Type", "text/csv")
        .body(resource);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
}
