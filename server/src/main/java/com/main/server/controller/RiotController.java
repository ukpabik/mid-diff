package com.main.server.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main.server.model.RiotUserType;
import com.main.server.service.RiotService;


/**
 * REST controller for Riot-related endpoints.
 * Routes API requests and delegates logic to RiotService.
 */
@RestController
@RequestMapping("/user")
public class RiotController {

  private RiotService riotService;


  public RiotController(RiotService riotService){
    this.riotService = riotService;
  }

  /**
   * GET /user/{riotId}/{tagLine}
   * 
   * Fetches a user's Riot account info based on Riot ID and tagline.
   * Example: /user/TheBestMid/NA1
   *
   * @param riotId the user's Riot game name (e.g., TheBestMid)
   * @param tagline the Riot tagLine (e.g., NA1)
   * @return RiotUser object or error message
   */
  @GetMapping("/{riotId}/{tagLine}")
  public ResponseEntity<?> getUser(@PathVariable String riotId, @PathVariable String tagLine) {
    try {
      RiotUserType user = riotService.getUserByRiotId(riotId, tagLine);
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
}
