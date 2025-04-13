package com.main.server.routes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.main.server.config.RiotConfig;


@RestController
public class RiotAPI {


  private final RiotConfig config = new RiotConfig();


  /**
   * 
   * TODO: Create endpoint for getting user information.
   * TODO: Create endpoint for retrieving match information.
   * 
   */

  @GetMapping(path= "/user/{riotId}/{tagline}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> getUser(@PathVariable String riotId, @PathVariable String tagline){
    JSONObject response;
    HttpURLConnection con;
    
    try{
      // establish our connection for get request
      URI uri = UriComponentsBuilder
      .fromUriString("https://americas.api.riotgames.com")
      .path("/riot/account/v1/accounts/by-riot-id/{riotId}/{tagline}")
      .queryParam("api_key", config.getKey())
      .buildAndExpand(riotId, tagline)
      .toUri();
      URL url = uri.toURL();
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Content-Type", "application/json");
      con.setConnectTimeout(5000);

      int status = con.getResponseCode();
      Reader streamReader;

      if (status > 299) {
        streamReader = new InputStreamReader(con.getErrorStream());
      } else {
        streamReader = new InputStreamReader(con.getInputStream());
      }
      BufferedReader in = new BufferedReader(streamReader);
      String inputLine;
      StringBuffer content = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
      in.close();
      con.disconnect();
      
      response = new JSONObject(content.toString());
      return new ResponseEntity<Object>(response.toMap(), HttpStatus.OK);
    }
    catch (Exception e){

      // Returns an error json object???
      response = new JSONObject(e.getMessage());
      return new ResponseEntity<Object>(response.toMap(), HttpStatus.INTERNAL_SERVER_ERROR);

    }
  }

}
