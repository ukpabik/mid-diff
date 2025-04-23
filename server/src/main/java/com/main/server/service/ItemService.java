package com.main.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class ItemService {

  // For caching items
  private static final int MAX_CACHE_SIZE = 100;

  private final RestTemplate rest;
  private String version;

  @SuppressWarnings("serial")
  private final Map<Integer,ItemDto> cache = Collections.synchronizedMap(
    new LinkedHashMap<Integer,ItemDto>(16, 0.75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<Integer,ItemDto> eldest) {
        return size() > MAX_CACHE_SIZE;
      }
    }
  );

  public ItemService(RestTemplateBuilder restBuilder) {
    this.rest = restBuilder
      .requestFactory(() -> {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(5000);
        return f;
      })
      .build();
  }

  // Gets the item and returns it if its not in map
  public ItemDto getItem(int id) {
    return cache.computeIfAbsent(id, this::fetchSingleItem);
  }
  
  // Returns ddragon version
  public synchronized String getVersion() {
    if (version == null) {
      @SuppressWarnings("unchecked")
      List<String> versions = rest.getForObject(
        "https://ddragon.leagueoflegends.com/api/versions.json", List.class);
      if (versions == null || versions.isEmpty()) {
        throw new IllegalStateException("No DataDragon versions returned");
      }
      version = versions.get(0);
    }
    return version;
  }

  
  private ItemDto fetchSingleItem(int id) {
    String url = String.format(
      "https://ddragon.leagueoflegends.com/cdn/%s/data/en_US/item/%d.json",
      getVersion(), id
    );
    JsonNode dataNode = rest.getForObject(url, JsonNode.class)
                           .path("data")
                           .path(String.valueOf(id));

    List<String> tags = new ArrayList<>();
    dataNode.path("tags").forEach(t -> tags.add(t.asText()));

    List<Integer> into = new ArrayList<>();
    dataNode.path("into").forEach(n -> into.add(n.asInt()));

    return new ItemDto(
      id,
      dataNode.path("name").asText(),
      dataNode.path("description").asText(),
      dataNode.path("gold").path("total").asInt(),
      dataNode.path("image").path("full").asText(),
      tags,
      into
    );
  }
}
