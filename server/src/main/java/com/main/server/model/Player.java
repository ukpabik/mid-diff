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

  @JsonProperty("last_synced")
  private String lastSynced;

  @JsonProperty("profile_picture")
  private String profileIconId;


  // Empty constructor for JBDC implementation
  public Player(){}

  public Player(String puuid, String gameName, String tagline, String profileIconId){
    this.puuid = puuid;
    this.gameName = gameName;
    this.tagLine = tagline;
    this.profileIconId = profileIconId;
  }


  public String getPuuid() { return puuid; }
  public String getGameName() { return gameName; }
  public String getTagLine() { return tagLine; }
  public String getProfileIconId() { return profileIconId; }

  // Setters
  public void setPuuid(String puuid) { this.puuid = puuid; }
  public void setGameName(String gameName) { this.gameName = gameName; }
  public void setTagLine(String tagLine) { this.tagLine = tagLine; }
  public void setProfileIconId(String profileIconId) { this.profileIconId = profileIconId; }

  @Override
  public String toString() {
    return gameName + "#" + tagLine + " (" + puuid + ") - Profile Picture: " + profileIconId;
  }
}
