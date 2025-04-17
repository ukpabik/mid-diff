package com.main.server.controller;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.main.server.model.Match;
import com.main.server.model.Player;
import com.main.server.model.RankInfo;
import com.main.server.service.DatabaseService;
import com.main.server.service.RiotService;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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

  private final Bucket bucket;

  @Value("${spring.api.backend.key}")
  private String backendApiKey;

  public Controller(DatabaseService databaseService, RiotService accountService){
    this.databaseService = databaseService;
    this.accountService = accountService;
    Bandwidth limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
    this.bucket = Bucket.builder()
    .addLimit(limit)
    .build();
  }

  /**
   * Determines whether a user is authorized to access endpoints.
   * @param request
   * @return True if the user is authenticated, false otherwise.
   */
  private boolean isAuthorized(HttpServletRequest request){
    String headerKey = request.getHeader("X-API-KEY");
    return headerKey != null && headerKey.equals(backendApiKey);
  }
  /**
   * Fetches a Riot user by game name and tagline using the Riot API.
   *
   * @param riotId the Riot game name (e.g., "TheBestMid")
   * @param tagLine the tagline associated with the Riot ID (e.g., "NA1")
   * @return {@link Player} object containing user info or error message on failure
   */
   @GetMapping("/{riotId}/{tagLine}/{region}")
  public ResponseEntity<?> getUser(@PathVariable String riotId, @PathVariable String tagLine, @PathVariable String region, HttpServletRequest request) {
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        Player user = accountService.getUserById(riotId, tagLine, region);
        return ResponseEntity.ok(user);
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Fetches a Riot user by puuid using Riot API.
   *
   * @param puuid Riot user puuid
   * @return {@link Player} object containing user info or error message on failure
   */
  @GetMapping("/lookup/{puuid}/{region}")
  public ResponseEntity<?> getUserByPuuid(@PathVariable String puuid, @PathVariable String region, HttpServletRequest request) {
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        Player user = accountService.getUserByPuuid(puuid, region);
        return ResponseEntity.ok(user);
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
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
  public ResponseEntity<?> getUserFromDb(@PathVariable String puuid, HttpServletRequest request) {
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        Player player = databaseService.findByPuuid(puuid);
        if (player != null){
          return ResponseEntity.ok(player);
        }
        throw new Exception("Not found");
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
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
  public ResponseEntity<?> upsertUser(@RequestBody Player user, HttpServletRequest request){
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        Player existingUser = databaseService.findByPuuid(user.getPuuid());
        if (existingUser == null) {
          databaseService.saveUser(user);
        } else {
          databaseService.updateUser(user);
        }
        return ResponseEntity.ok(user);
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
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
  @GetMapping("/search/{riotId}/{tagLine}/{platformRegion}/{routingRegion}")
  public ResponseEntity<?> searchAndCache(@PathVariable String riotId, @PathVariable String tagLine, @PathVariable String platformRegion, @PathVariable String routingRegion, HttpServletRequest request) {
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        Player user = accountService.getCompletePlayer(riotId, tagLine, platformRegion, routingRegion);
        Player existing = databaseService.findByPuuid(user.getPuuid());
        if (existing == null) {
          databaseService.saveUser(user);
        } else {
          databaseService.updateUser(user);
        }
        List<RankInfo> rankInfos = accountService.getRankInfoByPuuid(user.getPuuid(), platformRegion);
        for (RankInfo rankInfo : rankInfos){
          accountService.saveRankInfo(rankInfo);
        }
        List<String> ids = accountService.getRecentMatchIds(user.getPuuid(), "ranked", routingRegion, 20);
        accountService.cacheMissingMatchesAsync(ids, user.getPuuid(), routingRegion);
        return ResponseEntity.ok(user);
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
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
  public ResponseEntity<?> getCachedMatches(@PathVariable String puuid, HttpServletRequest request) {
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        return ResponseEntity.ok(accountService.getCachedMatches(puuid));
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
  

  /**
   * 
   * Fetches cached matches for a user and outputs a csv file with the match data. (For ML Model)
   * @param puuid the PUUID of the player
   * @param response csv file response
   */
  @GetMapping(value="/matches/csv/{puuid}", produces = "text/csv")
  public void downloadMatchCsv(@PathVariable String puuid, HttpServletResponse response, HttpServletRequest request){
    if (!isAuthorized(request)) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      return;
    }
    try {
      if (bucket.tryConsume(1)){
        List<Match> matches = accountService.getCachedMatches(puuid);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=match_history.csv");
        PrintWriter writer = response.getWriter();
        writer.println("puuid,matchId,championName,championId,teamPosition,win,kills,deaths,assists,goldEarned,goldSpent,csPerMin,kda,visionScore,wardsPlaced,wardsKilled,damageDealtToChampions,totalDamageTaken,gameMode,queueId,gameDuration,totalMinionsKilled,neutralMinionsKilled,turretTakedowns,inhibitorTakedowns");
        for (Match m : matches) {
          writer.printf("%s,%s,%s,%s,%s,%b,%d,%d,%d,%d,%d,%.15f,%.15f,%d,%d,%d,%d,%d,%s,%d,%d,%d,%d,%d,%d\n",
            m.getPuuid(), m.getMatchId(), m.getChampionName(), m.getChampionId(), m.getTeamPosition(), m.isWin(),
            m.getKills(), m.getDeaths(), m.getAssists(), m.getGoldEarned(), m.getGoldSpent(), m.getCsPerMin(),
            m.getKda(), m.getVisionScore(), m.getWardsPlaced(), m.getWardsKilled(), m.getDamageDealtToChampions(),
            m.getTotalDamageTaken(), m.getGameMode(), m.getQueueId(), m.getGameDuration(), m.getTotalMinionsKilled(),
            m.getNeutralMinionsKilled(), m.getTurretTakedowns(), m.getInhibitorTakedowns());
        }
        writer.flush();
      }
      else{
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      }
    } catch (Exception e) {
      response.setStatus(500);
    }
  }
  
  /**
   * Exports all cached matches across all users as a CSV file (for ML model training).
   *
   * @param response HttpServletResponse used to stream CSV data
   */
  @GetMapping(value = "/matches/csv/all", produces = "text/csv")
  public void downloadAllMatchesCsv(HttpServletResponse response, HttpServletRequest request) {
    if (!isAuthorized(request)) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      return;
    }
    try {
      if (bucket.tryConsume(1)){
        List<Match> matches = accountService.getAllMatches(); 
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=training_dataset.csv");
        PrintWriter writer = response.getWriter();
        writer.println("puuid,matchId,championName,championId,teamPosition,win,kills,deaths,assists,goldEarned,goldSpent,csPerMin,kda,visionScore,wardsPlaced,wardsKilled,damageDealtToChampions,totalDamageTaken,gameMode,queueId,gameDuration,totalMinionsKilled,neutralMinionsKilled,turretTakedowns,inhibitorTakedowns");
        for (Match m : matches) {
          writer.printf("%s,%s,%s,%s,%s,%b,%d,%d,%d,%d,%d,%.15f,%.15f,%d,%d,%d,%d,%d,%s,%d,%d,%d,%d,%d,%d\n",
            m.getPuuid(), m.getMatchId(), m.getChampionName(), m.getChampionId(), m.getTeamPosition(), m.isWin(),
            m.getKills(), m.getDeaths(), m.getAssists(), m.getGoldEarned(), m.getGoldSpent(), m.getCsPerMin(),
            m.getKda(), m.getVisionScore(), m.getWardsPlaced(), m.getWardsKilled(), m.getDamageDealtToChampions(),
            m.getTotalDamageTaken(), m.getGameMode(), m.getQueueId(), m.getGameDuration(), m.getTotalMinionsKilled(),
            m.getNeutralMinionsKilled(), m.getTurretTakedowns(), m.getInhibitorTakedowns());
        }
        writer.flush();
      }
      else{
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      }
    } catch (Exception e) {
      response.setStatus(500);
    }
  }


  /**
   * Retrieves a user's rank info by puuid.
   * @param puuid player id of user
   * @return A List of their rank infos.
   */
  @GetMapping("/rank/{puuid}/{region}")
  public ResponseEntity<?> getRankInfo(@PathVariable String puuid, @PathVariable String region, HttpServletRequest request) {
    if (!isAuthorized(request)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    try {
      if (bucket.tryConsume(1)){
        return ResponseEntity.ok(accountService.getRankInfoByPuuid(puuid, region));
      }
      else{
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
      }
      
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
}
