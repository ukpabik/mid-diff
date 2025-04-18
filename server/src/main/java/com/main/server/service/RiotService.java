package com.main.server.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.main.server.factory.Factory;
import com.main.server.model.Match;
import com.main.server.model.Player;
import com.main.server.model.PlayerBuild;
import com.main.server.model.RankInfo;
import com.main.server.repository.MatchRepository;
import com.main.server.repository.PlayerBuildRepository;
import com.main.server.repository.RankRepository;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.Bucket;

/**
 * Service responsible for interacting with Riot's external APIs to fetch and cache match data.
 * It also supports asynchronous caching and querying of existing match data in Supabase.
 */
@Service
public class RiotService {

  @Value("${spring.api.riot.key}")
  private String apiKey;

  @Autowired private MatchRepository matchRepository;
  @Autowired private RankRepository rankRepository;
  @Autowired private PlayerBuildRepository playerBuildRepository;

  private final ObjectMapper mapper = new ObjectMapper();

  private static final BlockingBucket RIOT_BUCKET = Bucket.builder()
    .addLimit(Bandwidth.simple(20,  Duration.ofSeconds(1)))
    .addLimit(Bandwidth.simple(100, Duration.ofMinutes(2)))
    .build()
    .asBlocking();


  /**
   * Executes the supplied {@link Callable} after consuming a single token from the bucket.  If the
   * callable throws {@link Riot429Exception} the helper waits the specified {@code Retry‑After}
   * interval, consumes another token and retries once.
   *
   * @param task blocking I/O task that performs the HTTP request
   * @param <T>  type of the value returned by the task
   * @return result of {@code task.call()}
   * @throws Exception any exception thrown by the task after one retry attempt
   */
  private <T> T withLimit(Callable<T> task) throws Exception {
      RIOT_BUCKET.consumeUninterruptibly(1);
      try {
        return task.call();
      } 
      catch (Riot429Exception e) {
        Thread.sleep(e.retryAfterSeconds() * 1_000L);
        RIOT_BUCKET.consumeUninterruptibly(1);
        return task.call();
      }
  }

  /**
   * Reads the full response body (success or error) from {@code con} into a single {@link String}.
  */
  private String readBody(HttpURLConnection con) throws Exception {
      Reader r = new InputStreamReader(
        con.getResponseCode() > 299 ? con.getErrorStream() : con.getInputStream());
      try (BufferedReader br = new BufferedReader(r)) {
        return br.lines().collect(Collectors.joining());
      }
  }


  /**
   * Performs a GET request to Riot, respecting rate limits and handling HTTP 429.
   *
   * @param uri Riot API URI, including the {@code api_key} query param
   * @return parsed {@link JsonNode} body
   * @throws Exception network I/O errors, JSON parse errors, or unrecoverable 429
   */
  private JsonNode riotGet(URI uri) throws Exception {
    return withLimit(() -> {
      HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Content-Type", "application/json");
      con.setConnectTimeout(5_000);

      int code = con.getResponseCode();
      if (code == 429) {
          long retry = Optional.ofNullable(con.getHeaderField("Retry-After"))
                                .map(Long::parseLong).orElse(1L);
          con.disconnect();
          throw new Riot429Exception(retry);
      }
      String body = readBody(con);
      con.disconnect();
      return mapper.readTree(body);
    });
  }

  /**
   * Fetches basic player identifiers (gameName, tagLine, PUUID) using Riot‑ID + tagline.
   *
   * @param id      Riot username (e.g. "Faker")
   * @param tagLine Tagline (e.g. "KR1")
   * @param region  Platform region (na1, euw1, …)
   * @return mapped {@link Player}
   */
  public Player getUserById(String id, String tagLine, String region) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://" + region + ".api.riotgames.com")
      .path("/riot/account/v1/accounts/by-riot-id/{id}/{tag}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(id, tagLine)
      .toUri();

    Map<String, Object> map = mapper.convertValue(riotGet(uri), new TypeReference<>() {});
    return Factory.mapToUser(map);
  }

  /**
   * Fetches basic player identifiers using a PUUID.
   *
   * @param puuid  Riot PUUID
   * @param region Platform region
   */
  public Player getUserByPuuid(String puuid, String region) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://" + region + ".api.riotgames.com")
      .path("/riot/account/v1/accounts/by-puuid/{puuid}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(puuid)
      .toUri();

    Map<String, Object> map = mapper.convertValue(riotGet(uri), new TypeReference<>() {});
    return Factory.mapToUser(map);
  }

  /**
   * Retrieves profile‑level data (icon ID, level, etc.) from Summoner‑V4 by PUUID.
   */
  public Player getUserProfileByPuuid(String puuid, String platformRegion) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://" + platformRegion + ".api.riotgames.com")
      .path("/lol/summoner/v4/summoners/by-puuid/{puuid}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(puuid)
      .toUri();

    Map<String,Object> map = mapper.convertValue(riotGet(uri), new TypeReference<>(){});
    return Factory.mapToUser(map);
  }

  public Player getCompletePlayer(String riotId, String tagLine, String platformRegion, String routingRegion) throws Exception {

    Player basic = getUserById(riotId, tagLine, routingRegion);

    Player profile = getUserProfileByPuuid(basic.getPuuid(), platformRegion);

    basic.setProfileIconId(profile.getProfileIconId());
    return basic;
  }

  /**
   * Returns all ranked entries (Solo/Duo, Flex, …) for the given PUUID.
   */
  public List<RankInfo> getRankInfoByPuuid(String puuid, String region) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://" + region + ".api.riotgames.com")
      .path("/lol/league/v4/entries/by-puuid/{puuid}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(puuid)
      .toUri();

    List<Map<String,Object>> list = mapper.readValue(
      riotGet(uri).traverse(),
      new TypeReference<>() {});

    List<RankInfo> out = new ArrayList<>();
    for (Map<String, Object> m : list) out.add(Factory.mapToRankInfo(m));
    return out;
  }


  /**
   * Returns the most‑recent match IDs for a player.
   *
   * @param puuid  Riot PUUID
   * @param type   Match type filter (e.g. "ranked")
   * @param region Routing region (americas, europe, …)
   * @param count  Max IDs to return (≤ 100)
   */
  public List<String> getRecentMatchIds(String puuid, String type,
                          String region, int count) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://" + region + ".api.riotgames.com")
      .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
      .queryParam("type", type)
      .queryParam("count", count)
      .queryParam("api_key", apiKey)
      .buildAndExpand(puuid)
      .toUri();

      return mapper.readValue(
        riotGet(uri).traverse(),
        new TypeReference<>() {});
  }

  /**
   * Downloads full match payload for the given ID.
   */
  public JsonNode getMatchById(String matchId, String region) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://" + region + ".api.riotgames.com")
      .path("/lol/match/v5/matches/{id}")
      .queryParam("api_key", apiKey)
      .buildAndExpand(matchId)
      .toUri();

    return riotGet(uri);
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

        PlayerBuild build = new PlayerBuild(
          m.getMatchId(),
          puuid,
          p.path("item0").asInt(),
          p.path("item1").asInt(),
          p.path("item2").asInt(),
          p.path("item3").asInt(),
          p.path("item4").asInt(),
          p.path("item5").asInt(),
          p.path("item6").asInt()
        );

        matchRepository.save(m);
        playerBuildRepository.save(build);
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
  public void cacheMissingMatches(List<String> ids, String puuid, String region) throws Exception {
    Set<String> existingMatches = matchRepository.findExistingMatchIdsForUser(ids, puuid);
    Set<String> existingBuilds  = playerBuildRepository.findMatchIdsByPuuid(puuid);

    for (String id : ids) {
        if (existingMatches.contains(id) && existingBuilds.contains(id)) {
          continue;
        }

        JsonNode matchJson = getMatchById(id, region);
        cacheMatch(matchJson, puuid);
    }
  } 

  /**
   * Caches matches that aren't saved in Supabase (asynchronously).
   * @param matchIds ids of matches pulled from request
   * @param puuid player id of the user
   * @param routingRegion region to search the matches from.
   * @return {@link CompletableFuture} if the job is finished.
   * @throws Exception
   */
  @Async("riotTaskExecutor")
  public CompletableFuture<Void> cacheMissingMatchesAsync(
          List<String> matchIds, String puuid, String routingRegion) throws Exception {
            
    cacheMissingMatches(matchIds, puuid, routingRegion);
    return CompletableFuture.completedFuture(null);
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

  /**
   * Takes the info and saves it into the database.
   * @param info a {@link RankInfo} object
   * @return A value indicating if it was successful or not
   */
  public int saveRankInfo(RankInfo info){
    return rankRepository.save(info);
  }


  /**
   * Wrapper exception thrown when Riot responds with HTTP 429.  Holds the {@code Retry‑After}
   * value (seconds) so the caller can back‑off accordingly.
   */
  public static class Riot429Exception extends Exception {
    private final long retryAfterSeconds;
    public Riot429Exception(long retryAfterSeconds) {
        super("HTTP 429 from Riot; retry in " + retryAfterSeconds + "s");
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public long retryAfterSeconds() { return retryAfterSeconds; }
  } 
}
