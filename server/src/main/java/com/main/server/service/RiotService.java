package com.main.server.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.main.server.config.RiotConfig;
import com.main.server.factory.RiotFactory;
import com.main.server.model.RiotUserType;

/**
 * RiotService handles communication with Riot's external APIs.
 * It processes raw data and constructs typed objects using the RiotFactory.
 */
@Service
public class RiotService {
  
  // Configuration object for loading Riot API key securely
  private final RiotConfig config = new RiotConfig();


  /**
   * Fetches Riot user account info from the Riot API using Riot ID + tagLine.
   *
   * @param riotId the user's Riot username (gameName)
   * @param tagLine the user's tagline (e.g., NA1)
   * @return RiotUserType object with gameName, tagLine, and puuid
   * @throws Exception if the API call fails or parsing goes wrong
   */
  public RiotUserType getUserByRiotId(String riotId, String tagLine) throws Exception {
    URI uri = UriComponentsBuilder
      .fromUriString("https://americas.api.riotgames.com")
      .path("/riot/account/v1/accounts/by-riot-id/{riotId}/{tagline}")
      .queryParam("api_key", config.getKey())
      .buildAndExpand(riotId, tagLine)
      .toUri();

    URL url = uri.toURL();
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setConnectTimeout(5000);

    Reader streamReader = (con.getResponseCode() > 299)
      ? new InputStreamReader(con.getErrorStream())
      : new InputStreamReader(con.getInputStream());

    BufferedReader in = new BufferedReader(streamReader);
    StringBuilder content = new StringBuilder();
    String inputLine;

    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }

    in.close();
    con.disconnect();

    Map<String, Object> map = new JSONObject(content.toString()).toMap();
    return RiotFactory.mapToUser(map);
  }
}
