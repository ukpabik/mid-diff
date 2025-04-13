package com.main.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Riot account with key identifying fields.
 * This is the domain model used throughout the app instead of raw API data.
 */
public class Player {
  @JsonProperty("puuid")
  private String puuid;

  @JsonProperty("game_name")
  private String gameName;

  @JsonProperty("tag_line")
  private String tagLine;


  // Empty constructor for JBDC implementation
  public Player(){}

  public Player(String puuid, String gameName, String tagline){
    this.puuid = puuid;
    this.gameName = gameName;
    this.tagLine = tagline;
  }


  // Getters
  public String getPuuid() { return puuid; }
  public String getGameName() { return gameName; }
  public String getTagLine() { return tagLine; }

  // Setters
  public void setPuuid(String puuid) { this.puuid = puuid; }
  public void setGameName(String gameName) { this.gameName = gameName; }
  public void setTagLine(String tagline) { this.tagLine = tagline; }

  @Override
  public String toString() {
      return gameName + "#" + tagLine + " (" + puuid + ")";
  }
}
