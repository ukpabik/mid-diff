package com.main.server.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.main.server.factory.Factory;
import com.main.server.model.Match;
import com.main.server.model.Player;
import com.main.server.repository.MatchRepository;

/**
 * Service responsible for interacting with Riot's external APIs to fetch and cache match data.
 * It also supports asynchronous caching and querying of existing match data in Supabase.
 */
@Service
public class RiotService {

  @Value("${spring.api.riot.key}")
  private String apiKey;
  @Autowired
  private MatchRepository matchRepository;

  private final ObjectMapper mapper = new ObjectMapper();
  

  /**
   * Fetches basic player data (gameName, tagLine, puuid) from Riot API using Riot ID and tagline.
   *
   * @param id      the Riot username (e.g., "Faker")
   * @param tagLine the user's tagline (e.g., "KR1")
   * @return the {@link Player} object with Riot identifiers
   * @throws Exception if the Riot API request or parsing fails
   */
  public Player getUserById(String id, String tagLine) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://americas.api.riotgames.com")
      .path("/riot/account/v1/accounts/by-riot-id/{riotId}/{tagline}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(id, tagLine)
      .toUri();

    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setConnectTimeout(5000);

    Reader streamReader = (con.getResponseCode() > 299)
      ? new InputStreamReader(con.getErrorStream())
      : new InputStreamReader(con.getInputStream());

    StringBuilder content = new StringBuilder();
    try (BufferedReader in = new BufferedReader(streamReader)) {
      String line;
      while ((line = in.readLine()) != null) {
          content.append(line);
      }
    }
    con.disconnect();
    @SuppressWarnings("unchecked")
    Map<String, Object> map = mapper.readValue(content.toString(), Map.class);
    return Factory.mapToUser(map);
  }

  /**
   * Fetches basic player data (gameName, tagLine, puuid) from Riot API using their puuid.
   *
   * @param puuid
   * @return the {@link Player} object with Riot identifiers
   * @throws Exception if the Riot API request or parsing fails
   */
  public Player getUserByPuuid(String puuid) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://americas.api.riotgames.com")
      .path("/riot/account/v1/accounts/by-puuid/{puuid}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(puuid)
      .toUri();
  
    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setConnectTimeout(5000);
  
    Reader streamReader = (con.getResponseCode() > 299)
      ? new InputStreamReader(con.getErrorStream())
      : new InputStreamReader(con.getInputStream());
  
    StringBuilder content = new StringBuilder();
    try (BufferedReader in = new BufferedReader(streamReader)) {
      String line;
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
    }
    con.disconnect();
  
    @SuppressWarnings("unchecked")
    Map<String, Object> map = mapper.readValue(content.toString(), Map.class);
  
    return Factory.mapToUser(map);
  }

  /**
   * Retrieves user profile info by puuid (summoner icon, level, etc)
   * @param puuid
   * @return
   * @throws Exception
   */
  public Player getUserProfileByPuuid(String puuid) throws Exception {
    URI uri = UriComponentsBuilder
        .fromUriString("https://na1.api.riotgames.com")
        .path("/lol/summoner/v4/summoners/by-puuid/{puuid}")
        .queryParam("api_key", apiKey)
        .buildAndExpand(puuid)
        .toUri();
  
    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setConnectTimeout(5000);
  
    Reader streamReader = (con.getResponseCode() > 299)
        ? new InputStreamReader(con.getErrorStream())
        : new InputStreamReader(con.getInputStream());
  
    StringBuilder content = new StringBuilder();
    try (BufferedReader in = new BufferedReader(streamReader)) {
      String line;
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
    }
    con.disconnect();
  
    @SuppressWarnings("unchecked")
    Map<String, Object> map = mapper.readValue(content.toString(), Map.class);
  
    // The Factory now maps the profileIconId into a profilePicture URL if available.
    return Factory.mapToUser(map);
  }


  /**
   * Returns the full profile with profile icon id for displaying summoner icons.
   * 
   * @param riotId
   * @param tagLine
   * @return
   * @throws Exception
   */
  public Player getCompletePlayer(String riotId, String tagLine) throws Exception {
    // Get basic player details
    Player basicPlayer = getUserById(riotId, tagLine);
    if (basicPlayer == null) {
      throw new Exception("Basic player data not found");
    }
    // Get extended details (profile icon) using puuid.
    Player extendedPlayer = getUserProfileByPuuid(basicPlayer.getPuuid());

    basicPlayer.setProfileIconId(extendedPlayer.getProfileIconId());
    return basicPlayer;
  }
  
  

  /**
   * Fetches the most recent match IDs for a given player from Riot.
   *
   * @param puuid the player's Riot PUUID
   * @param type  the match type (e.g., "ranked")
   * @param count the number of match IDs to retrieve
   * @return a list of match ID strings
   * @throws Exception if the Riot API request or parsing fails
   */
  public List<String> getRecentMatchIds(String puuid, String type, int count) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://americas.api.riotgames.com")
      .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
      .queryParam("api_key", apiKey)
      .queryParam("type", type)
      .queryParam("count", count)
      .buildAndExpand(puuid)
      .toUri();

    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setConnectTimeout(5000);

    Reader streamReader = (con.getResponseCode() > 299)
      ? new InputStreamReader(con.getErrorStream())
      : new InputStreamReader(con.getInputStream());

    StringBuilder content = new StringBuilder();
    try (BufferedReader in = new BufferedReader(streamReader)) {
      String line;
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
    }
    con.disconnect();

    return mapper.readValue(content.toString(), new TypeReference<List<String>>() {});
  }

  /**
   * Retrieves full match data from Riot API using the match ID.
   *
   * @param matchId the match ID string
   * @return a {@link JsonNode} representing the entire match payload
   * @throws Exception if the Riot API request or parsing fails
   */
  public JsonNode getMatchById(String matchId) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://americas.api.riotgames.com")
      .path("/lol/match/v5/matches/{matchId}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(matchId)
      .toUri();

    HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setConnectTimeout(5000);

    Reader streamReader = (con.getResponseCode() > 299)
      ? new InputStreamReader(con.getErrorStream())
      : new InputStreamReader(con.getInputStream());

    StringBuilder content = new StringBuilder();
    try (BufferedReader in = new BufferedReader(streamReader)) {
      String line;
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
    }
    con.disconnect();

    return mapper.readTree(content.toString());
  }

  /**
   * Parses a match JSON and saves it to the DB if the player participated.
   *
   * @param matchJson the full match JSON from Riot API
   * @param puuid     the target player's Riot PUUID
   */

  public void cacheMatch(JsonNode matchJson, String puuid) {
    JsonNode info = matchJson.get("info");
    JsonNode meta = matchJson.get("metadata");

    // find the participant for the given puuid
    for (JsonNode p : info.get("participants")) {
      if (p.get("puuid").asText().equals(puuid)) {
        Match m = new Match();
        m.setMatchId(meta.path("matchId").asText());
        m.setPuuid(puuid);
        m.setChampionName(p.path("championName").asText());
        m.setChampionId(p.path("championId").asText());
        m.setTeamPosition(p.path("teamPosition").asText());
        m.setWin(p.path("win").asBoolean());

        m.setKills(p.path("kills").asInt());
        m.setDeaths(p.path("deaths").asInt());
        m.setAssists(p.path("assists").asInt());

        m.setGoldEarned(p.path("goldEarned").asInt());
        m.setGoldSpent(p.path("goldSpent").asInt());

        m.setTotalMinionsKilled(p.path("totalMinionsKilled").asInt());
        m.setNeutralMinionsKilled(p.path("neutralMinionsKilled").asInt());

        m.setDamageDealtToChampions(p.path("totalDamageDealtToChampions").asInt());
        m.setTotalDamageTaken(p.path("totalDamageTaken").asInt());

        m.setVisionScore(p.path("visionScore").asInt());
        m.setWardsPlaced(p.path("wardsPlaced").asInt());
        m.setWardsKilled(p.path("wardsKilled").asInt());

        m.setTurretTakedowns(p.path("turretTakedowns").asInt());
        m.setInhibitorTakedowns(p.path("inhibitorTakedowns").asInt());

        m.setGameStartTimestamp(info.path("gameStartTimestamp").asLong());
        m.setGameDuration(info.path("gameDuration").asLong());
        m.setGameMode(info.path("gameMode").asText());
        m.setQueueId(info.path("queueId").asInt());

        double totalCS = m.getTotalMinionsKilled() + m.getNeutralMinionsKilled();
        double gameDurationSec = info.path("gameDuration").asDouble(); 
        double csPerMin = totalCS / (gameDurationSec / 60.0);
        m.setCsPerMin(csPerMin);

        double kda = (m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths());
        m.setKda(kda);

        matchRepository.save(m);
      }
    }
  }
  /**
   * Caches only matches that aren't already saved in Supabase.
   *
   * @param ids   list of match IDs
   * @param puuid the player's PUUID
   * @throws Exception if any match fails to fetch
   */
  public void cacheMissingMatches(List<String> ids, String puuid) throws Exception {
    Set<String> existing = matchRepository.findExistingMatchIds(ids);

    for (String id : ids) {
      if (!existing.contains(id)) {
        JsonNode match = getMatchById(id);
        cacheMatch(match, puuid);
      }
    }
  }

  /**
   * Fetches all cached matches for a given PUUID from the local database.
   *
   * @param puuid the player's Riot PUUID
   * @return a list of cached {@link Match} records
   */
  public List<Match> getCachedMatches(String puuid) {
    return matchRepository.findByPuuid(puuid);
  }

  /**
   * Fetches all matches in the database.
   *
   * @return a list of all {@link Match} records
   */
  public List<Match> getAllMatches(){
    return matchRepository.findAll();
  }

  
}
