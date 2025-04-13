package com.main.server.factory;

import java.util.Map;

import com.main.server.model.RiotUserType;

/**
 * Factory class responsible for building Riot type objects
 * from raw data sources like Riot API responses.
 */
public class RiotFactory {

  /**
   * Converts a Map (parsed from a Riot API JSON response)
   * into a RiotUserType domain object.
   *
   * @param data a map containing Riot API user fields
   * @return a fully-constructed RiotUserType
   */
  public static RiotUserType mapToUser(Map<String, Object> data){
    String puuid = (String) data.get("puuid");
    String gameName = (String) data.get("gameName");
    String tagLine = (String) data.get("tagLine");

    return new RiotUserType(puuid, gameName, tagLine);
  }
}
