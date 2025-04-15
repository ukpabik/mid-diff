package com.main.server.factory;

import java.util.Map;

import com.main.server.model.Player;
import com.main.server.model.RankInfo;

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
    String gameName = (String) data.get("gameName");
    String tagLine = (String) data.get("tagLine");
    String profilePicture = null;
    if (data.containsKey("profileIconId")) {
      Object iconObj = data.get("profileIconId");
      try {
        int profileIconId = (iconObj instanceof Number)
            ? ((Number) iconObj).intValue()
            : Integer.parseInt(iconObj.toString());
        profilePicture = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/" + profileIconId + ".jpg";
      } catch (NumberFormatException e) {
        System.err.println("Error parsing profileIconId: " + iconObj);
      }
    }

    return new Player(puuid, gameName, tagLine, profilePicture);
  }

  /**
 * Converts a Map (parsed from one JSON object in the rank info response)
 * into a RankInfo domain object.
 *
 * @param data a map containing rank info fields from the API
 * @return a fully-constructed RankInfo object
 */
  public static RankInfo mapToRankInfo(Map<String, Object> data) {
    String queueType = (String) data.get("queueType");
    String tier = (String) data.get("tier");
    String rank = (String) data.get("rank");
    String puuid = (String) data.get("puuid");
    
    int leaguePoints = data.get("leaguePoints") instanceof Number
            ? ((Number) data.get("leaguePoints")).intValue()
            : Integer.parseInt(data.get("leaguePoints").toString());
    int wins = data.get("wins") instanceof Number
            ? ((Number) data.get("wins")).intValue()
            : Integer.parseInt(data.get("wins").toString());
    int losses = data.get("losses") instanceof Number
            ? ((Number) data.get("losses")).intValue()
            : Integer.parseInt(data.get("losses").toString());
    
    return new RankInfo(puuid, queueType, tier, rank, leaguePoints, wins, losses);
  }
}
