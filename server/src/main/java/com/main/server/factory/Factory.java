package com.main.server.factory;

import java.util.Map;

import com.main.server.model.Player;

/**
 * Factory class responsible for building Riot type objects
 * from raw data sources like Riot API responses.
 */
public class Factory {
  /**
   * Converts a Map (parsed from a Riot API JSON response)
   * into a UserType domain object.
   *
   * @param data a map containing Riot API user fields
   * @return a fully-constructed UserType
   */
  public static Player mapToUser(Map<String, Object> data){
    String puuid = (String) data.get("puuid");
    String gameName = (String) data.get("game_name");
    String tagLine = (String) data.get("tag_line");

    return new Player(puuid, gameName, tagLine);
  }
}
