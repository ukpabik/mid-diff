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
}
