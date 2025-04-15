package com.main.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the ranked information for a League of Legends player.
 * This model includes data from different ranked queues and is linked
 * to the player via the puuid.
 */
public class RankInfo {

  @JsonProperty("puuid")
  private String puuid;

  @JsonProperty("queueType")
  private String queueType;
  
  @JsonProperty("tier")
  private String tier;
  
  @JsonProperty("player_rank")
  private String rank;
  
  @JsonProperty("leaguePoints")
  private int leaguePoints;
  
  @JsonProperty("wins")
  private int wins;
  
  @JsonProperty("losses")
  private int losses;

  public RankInfo() {
  }

  public RankInfo(String puuid, String queueType, String tier, String rank, int leaguePoints, int wins, int losses) {
    this.puuid = puuid;
    this.queueType = queueType;
    this.tier = tier;
    this.rank = rank;
    this.leaguePoints = leaguePoints;
    this.wins = wins;
    this.losses = losses;
  }

  // Getters and Setters
  public String getPuuid() {
    return puuid;
  }

  public void setPuuid(String puuid) {
    this.puuid = puuid;
  }

  public String getQueueType() {
    return queueType;
  }

  public void setQueueType(String queueType) {
    this.queueType = queueType;
  }

  public String getTier() {
    return tier;
  }

  public void setTier(String tier) {
    this.tier = tier;
  }

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public int getLeaguePoints() {
    return leaguePoints;
  }

  public void setLeaguePoints(int leaguePoints) {
    this.leaguePoints = leaguePoints;
  }

  public int getWins() {
    return wins;
  }

  public void setWins(int wins) {
    this.wins = wins;
  }

  public int getLosses() {
    return losses;
  }

  public void setLosses(int losses) {
    this.losses = losses;
  }

  @Override
  public String toString() {
    return "RankInfo{" +
      "puuid='" + puuid + '\'' +
      ", queueType='" + queueType + '\'' +
      ", tier='" + tier + '\'' +
      ", rank='" + rank + '\'' +
      ", leaguePoints=" + leaguePoints +
      ", wins=" + wins +
      ", losses=" + losses +
      '}';
  }
}
