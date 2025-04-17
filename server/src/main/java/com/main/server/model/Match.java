package com.main.server.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a parsed League of Legends match specific to a single player (by PUUID).
 * 
 * This model is built from Riot's Match API (/lol/match/v5/matches/{matchId}) and is designed
 * to store the most relevant attributes.
 */
public class Match {
  @JsonAlias({ "match_id", "metadata.matchId" })
  @JsonProperty("metadata.matchId")
  private String matchId;

  @JsonProperty("puuid")
  private String puuid;

  @JsonProperty("championName")
  private String championName;

  @JsonProperty("championId")
  private String championId;

  @JsonProperty("teamPosition")
  private String teamPosition;

  @JsonProperty("win")
  private boolean win;

  @JsonProperty("kills")
  private int kills;

  @JsonProperty("deaths")
  private int deaths;

  @JsonProperty("assists")
  private int assists;

  @JsonProperty("goldEarned")
  private int goldEarned;

  @JsonProperty("goldSpent")
  private int goldSpent;

  @JsonProperty("totalMinionsKilled")
  private int totalMinionsKilled;

  @JsonProperty("neutralMinionsKilled")
  private int neutralMinionsKilled;

  @JsonProperty("damageDealtToChampions")
  private int damageDealtToChampions;

  @JsonProperty("totalDamageTaken")
  private int totalDamageTaken;

  @JsonProperty("visionScore")
  private int visionScore;

  @JsonProperty("wardsPlaced")
  private int wardsPlaced;

  @JsonProperty("wardsKilled")
  private int wardsKilled;

  @JsonProperty("turretTakedowns")
  private int turretTakedowns;

  @JsonProperty("inhibitorTakedowns")
  private int inhibitorTakedowns;

  @JsonProperty("gameStartTimestamp")
  private long gameStartTimestamp;

  @JsonProperty("gameDuration")
  private long gameDuration;

  @JsonProperty("gameMode")
  private String gameMode;

  @JsonProperty("queueId")
  private int queueId;

  @JsonProperty("csPerMin")
  private double csPerMin;

  @JsonProperty("kda")
  private double kda;

  public Match() {}

  // Getters and Setters
  public String getMatchId() { return matchId; }
  public void setMatchId(String matchId) { this.matchId = matchId; }

  public String getPuuid() { return puuid; }
  public void setPuuid(String puuid) { this.puuid = puuid; }

  public String getChampionName() { return championName; }
  public void setChampionName(String championName) { this.championName = championName; }

  public String getChampionId() { return championId; }
  public void setChampionId(String championId) { this.championId = championId; }

  public String getTeamPosition() { return teamPosition; }
  public void setTeamPosition(String teamPosition) { this.teamPosition = teamPosition; }

  public boolean isWin() { return win; }
  public void setWin(boolean win) { this.win = win; }

  public int getKills() { return kills; }
  public void setKills(int kills) { this.kills = kills; }

  public int getDeaths() { return deaths; }
  public void setDeaths(int deaths) { this.deaths = deaths; }

  public int getAssists() { return assists; }
  public void setAssists(int assists) { this.assists = assists; }

  public int getGoldEarned() { return goldEarned; }
  public void setGoldEarned(int goldEarned) { this.goldEarned = goldEarned; }

  public int getGoldSpent() { return goldSpent; }
  public void setGoldSpent(int goldSpent) { this.goldSpent = goldSpent; }

  public int getTotalMinionsKilled() { return totalMinionsKilled; }
  public void setTotalMinionsKilled(int totalMinionsKilled) { this.totalMinionsKilled = totalMinionsKilled; }

  public int getNeutralMinionsKilled() { return neutralMinionsKilled; }
  public void setNeutralMinionsKilled(int neutralMinionsKilled) { this.neutralMinionsKilled = neutralMinionsKilled; }

  public int getDamageDealtToChampions() { return damageDealtToChampions; }
  public void setDamageDealtToChampions(int damageDealtToChampions) { this.damageDealtToChampions = damageDealtToChampions; }

  public int getTotalDamageTaken() { return totalDamageTaken; }
  public void setTotalDamageTaken(int totalDamageTaken) { this.totalDamageTaken = totalDamageTaken; }

  public int getVisionScore() { return visionScore; }
  public void setVisionScore(int visionScore) { this.visionScore = visionScore; }

  public int getWardsPlaced() { return wardsPlaced; }
  public void setWardsPlaced(int wardsPlaced) { this.wardsPlaced = wardsPlaced; }

  public int getWardsKilled() { return wardsKilled; }
  public void setWardsKilled(int wardsKilled) { this.wardsKilled = wardsKilled; }

  public int getTurretTakedowns() { return turretTakedowns; }
  public void setTurretTakedowns(int turretTakedowns) { this.turretTakedowns = turretTakedowns; }

  public int getInhibitorTakedowns() { return inhibitorTakedowns; }
  public void setInhibitorTakedowns(int inhibitorTakedowns) { this.inhibitorTakedowns = inhibitorTakedowns; }

  public long getGameStartTimestamp() { return gameStartTimestamp; }
  public void setGameStartTimestamp(long gameStartTimestamp) { this.gameStartTimestamp = gameStartTimestamp; }

  public long getGameDuration() { return gameDuration; }
  public void setGameDuration(long gameDuration) { this.gameDuration = gameDuration; }

  public String getGameMode() { return gameMode; }
  public void setGameMode(String gameMode) { this.gameMode = gameMode; }

  public int getQueueId() { return queueId; }
  public void setQueueId(int queueId) { this.queueId = queueId; }

  public double getCsPerMin() { return csPerMin; }
  public void setCsPerMin(double csPerMin) { this.csPerMin = csPerMin; }

  public double getKda() { return kda; }
  public void setKda(double kda) { this.kda = kda; }

  @Override
  public String toString() {
    return "Match{" +
      "matchId='" + matchId + '\'' +
      ", puuid='" + puuid + '\'' +
      ", championName='" + championName + '\'' +
      ", championId='" + championId + '\'' +
      ", teamPosition='" + teamPosition + '\'' +
      ", win=" + win +
      ", kills=" + kills +
      ", deaths=" + deaths +
      ", assists=" + assists +
      ", goldEarned=" + goldEarned +
      ", goldSpent=" + goldSpent +
      ", csPerMin=" + csPerMin +
      ", kda=" + kda +
      '}';
  }
}
