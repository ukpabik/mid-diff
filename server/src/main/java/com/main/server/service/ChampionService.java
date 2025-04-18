package com.main.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;

/**
 * Service used to get champion info (specifically tags information).
 */
@Service
public class ChampionService {
  private final Map<Integer, ChampionDto> champions = new ConcurrentHashMap<>();
  private String ddragonVersion;

  @SuppressWarnings("unchecked")
  @PostConstruct
  public void init() {
    RestTemplate rest = new RestTemplate();

    // fetch DDragon versions
    List<String> versions = rest.getForObject(
      "https://ddragon.leagueoflegends.com/api/versions.json",
      List.class
    );
    ddragonVersion = versions.get(0);

    // load champion.json
    String url = String.format(
      "https://ddragon.leagueoflegends.com/cdn/%s/data/en_US/champion.json",
      ddragonVersion
    );
    JsonNode root = rest.getForObject(url, JsonNode.class);
    JsonNode data = root.path("data");

    // build our map of tags
    data.fields().forEachRemaining(entry -> {
      JsonNode champ = entry.getValue();
      int id = champ.path("key").asInt();
      String name = champ.path("name").asText();

      JsonNode tagsNode = champ.path("tags");
      List<String> tagList = new ArrayList<>();
      if (tagsNode.isArray()) {
        for (JsonNode t : tagsNode) {
          tagList.add(t.asText());
        }
      }

      champions.put(id, new ChampionDto(id, name, tagList));
    });

    
  }

  /**
   * 
   * Returns the primary role for a specific champion.
   * @param championId
   * @return
   */
  public String getRoleForChampionId(int championId) {
    ChampionDto dto = champions.get(championId);
    if (dto == null || dto.getTags().isEmpty()) {
      return "Unknown";
    }
    return dto.getTags().get(0);
  }

  public String getVersion() {
    return ddragonVersion;
  }
}