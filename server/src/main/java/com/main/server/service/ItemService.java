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
 * Service class for loading Data Dragon metadata on application startup.
 */
@Service
public class ItemService {
  private final Map<Integer, ItemDto> items = new ConcurrentHashMap<>();
  private String version;

  @SuppressWarnings("unchecked")
  @PostConstruct
  public void init() {
    RestTemplate rest = new RestTemplate();
    // Get versions of ddragon for items
    List<String> versions = rest.getForObject(
      "https://ddragon.leagueoflegends.com/api/versions.json", List.class);
    version = versions.get(0);

    // Fetch list of all items
    String url = String.format(
      "https://ddragon.leagueoflegends.com/cdn/%s/data/en_US/item.json",
      version);
    JsonNode root = rest.getForObject(url, JsonNode.class);
    JsonNode data = root.get("data");

    // Map each item to its item id
    data.fields().forEachRemaining(e -> {
      int id = Integer.parseInt(e.getKey());
      JsonNode node = e.getValue();
      List<String> tags = new ArrayList<>();
      JsonNode tagsNode = node.get("tags");
      if (tagsNode != null && tagsNode.isArray()) {
        for (JsonNode t : tagsNode) {
          tags.add(t.asText());
        }
      }
      List<Integer> into = new ArrayList<>();
      if (node.has("into")) {
        for (JsonNode intoId : node.get("into")) {
          into.add(intoId.asInt());
        }
      }
      items.put(id, new ItemDto(
        id,
        node.get("name").asText(),
        node.get("description").asText(),
        node.get("gold").get("total").asInt(),
        node.get("image").get("full").asText(),
        tags,
        into
      ));
    });
  }

  /** Returns null if the ID isn’t known. */
  public ItemDto getItem(int id) {
    return items.get(id);
  }

  /** The version to build icon URLs. */
  public String getVersion() {
    return version;
  }
}

