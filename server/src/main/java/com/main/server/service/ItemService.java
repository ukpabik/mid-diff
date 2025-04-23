package com.main.server.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.PostConstruct;



/**
 * Service class for loading Data Dragon metadata on application startup.
 */
@Service
public class ItemService {
  private final Cache<Integer,ItemDto> items;
  private String version;
  private final RestTemplate rest;
  private final ObjectMapper mapper;



  public ItemService(RestTemplateBuilder restBuilder) {
    this.items = Caffeine.newBuilder()
      .maximumSize(200)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

    this.rest = restBuilder
      .requestFactory(() -> {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        return factory;
      })
      .build();
    this.mapper = new ObjectMapper();
  }


  @SuppressWarnings("unchecked")
  @PostConstruct
  public void init() {
    // Only fetch the DataDragon version on startup
    List<String> versions = rest.getForObject(
        "https://ddragon.leagueoflegends.com/api/versions.json",
        List.class
    );
    if (versions == null || versions.isEmpty()) {
      throw new IllegalStateException("Failed to fetch DDragon versions");
    }
    this.version = versions.get(0);
  }


  public ItemDto getItem(int id) {
    return items.get(id, this::loadSingleItem);
  }

  /**
   * Loader method: streams through the full JSON but only materializes
   * the one node you asked for. If it’s not found, returns null.
   */
  private ItemDto loadSingleItem(int id) {
    String url = String.format(
      "https://ddragon.leagueoflegends.com/cdn/%s/data/en_US/item.json",
      version
    );

    return rest.execute(
      url,
      HttpMethod.GET,
      null,
      clientResponse -> {
        JsonFactory factory = mapper.getFactory();
        try (JsonParser p = factory.createParser(clientResponse.getBody())) {
          // Advance to the "data" field
          while (p.nextToken() != JsonToken.FIELD_NAME || !"data".equals(p.currentName()));
          p.nextToken();

          // Iterate entries until we find our key
          while (p.nextToken() == JsonToken.FIELD_NAME) {
            String key = p.currentName();
            p.nextToken();
            if (Integer.toString(id).equals(key)) {
              JsonNode node = mapper.readTree(p);

              List<String> tags = StreamSupport.stream(
                node.path("tags").spliterator(), false
              )
              .map(JsonNode::asText)
              .collect(Collectors.toList());

              List<Integer> into = StreamSupport.stream(
                node.path("into").spliterator(), false
              )
              .map(JsonNode::asInt)
              .collect(Collectors.toList());

              return new ItemDto(
                id,
                node.path("name").asText(),
                node.path("description").asText(),
                node.path("gold").path("total").asInt(),
                node.path("image").path("full").asText(),
                tags,
                into
              );
            } else {
              p.skipChildren();
            }
          }
        }
        return null;
      }
    );
  }

  public String getVersion() {
    return this.version;
  }
}

