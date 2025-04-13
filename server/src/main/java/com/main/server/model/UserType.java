package com.main.server.model;

/**
 * Represents a Riot account with key identifying fields.
 * This is the domain model used throughout the app instead of raw API data.
 */
public class UserType {
  private String puuid;
  private String gameName;
  private String tagline;

  public UserType(String puuid, String gameName, String tagline){
    this.puuid = puuid;
    this.gameName = gameName;
    this.tagline = tagline;
  }

  public String getPuuid() { return puuid; }
  public String getGameName() { return gameName; }
  public String getTagLine() { return tagline; }

  @Override
  public String toString() {
      return gameName + "#" + tagline + " (" + puuid + ")";
  }
}
