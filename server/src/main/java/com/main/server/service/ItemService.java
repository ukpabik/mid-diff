package com.main.server.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
  private final TaskExecutor taskExecutor;
  private final RestTemplateBuilder restBuilder;



  public ItemService(
    TaskExecutor taskExecutor,
    RestTemplateBuilder restBuilder
  ) {
    this.taskExecutor = taskExecutor;
    this.restBuilder = restBuilder;
  }


  @PostConstruct
  public void init() {
    taskExecutor.execute(this::loadAllItemsInBackground);
  }
  @SuppressWarnings("unchecked")
  private void loadAllItemsInBackground() {
    try {
      RestTemplate rest = restBuilder
        .requestFactory(() -> {
          SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
          factory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
          factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
          return factory;
        })
        .build();

      // 1) fetch DDragon versions
      List<String> versions = rest.getForObject(
        "https://ddragon.leagueoflegends.com/api/versions.json",
        List.class
      );
      if (versions == null || versions.isEmpty()) {
        throw new IllegalStateException("No DataDragon versions returned");
      }
      version = versions.get(0);

      // 2) fetch all items
      String url = String.format(
        "https://ddragon.leagueoflegends.com/cdn/%s/data/en_US/item.json",
        version
      );
      JsonNode root = rest.getForObject(url, JsonNode.class);
      JsonNode data = root.path("data");

      // 3) map each item into our DTO
      data.fields().forEachRemaining(entry -> {
        int id = Integer.parseInt(entry.getKey());
        JsonNode node = entry.getValue();

        // tags array
        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = node.path("tags");
        if (tagsNode.isArray()) {
          tagsNode.forEach(t -> tags.add(t.asText()));
        }

        // into array (children) → used to detect end-tier items
        List<Integer> into = new ArrayList<>();
        JsonNode intoNode = node.path("into");
        if (intoNode.isArray()) {
          intoNode.forEach(n -> into.add(n.asInt()));
        }

        // build and store our DTO
        items.put(id, new ItemDto(
          id,
          node.path("name").asText(),
          node.path("description").asText(),
          node.path("gold").path("total").asInt(),
          node.path("image").path("full").asText(),
          tags,
          into
        ));
      });

      System.out.println("✅ DataDragon metadata loaded: " + items.size() + " items.");

    } catch (Exception e) {
      // Log and continue—app stays up, just no item data until it eventually loads
      System.err.println("⚠️ Failed to load DataDragon metadata: " + e.getMessage());
    }
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

