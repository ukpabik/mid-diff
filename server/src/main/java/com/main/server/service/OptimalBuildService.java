package com.main.server.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.main.server.repository.ChampionItemWinrateRepo;
import com.main.server.stats.ChampionItemWinrate;

/**
 * Service that computes an "optimal" 7-item build for a given champion
 * based on historical win rates, while filtering out non-raw items.
 */
@Service
public class OptimalBuildService {

  private final ChampionItemWinrateRepo winRepo;
  private final ItemService             itemService;

  public OptimalBuildService(
      ChampionItemWinrateRepo winRepo,
      ItemService itemService
  ) {
    this.winRepo     = winRepo;
    this.itemService = itemService;
  }

  /**
   * Retrieves the top 7 items for the given champion, sorted by descending win rate.
   * Excludes items tagged as Consumable, Trinket, and any items starting with "Doran".
   * If fewer than 7 remain after filtering, fills the remaining slots using a fallback list.
   *
   * @param champion Name of the champion (case-insensitive)
   * @return List of ItemDto objects (up to 7) representing the recommended build
   */
  public List<ItemDto> getOptimalBuild(String champion) {
    List<ChampionItemWinrate> rows = 
      winRepo.findByChampionNameIgnoreCase(champion);

    List<ChampionItemWinrate> filtered = rows.stream()
      .filter(r -> {
          int id = r.getItemId();
          ItemDto dto = itemService.getItem(id);
          if (dto == null) {
            return false;
          }
          var tags = dto.getTags();
          if (tags.contains("Consumable") || tags.contains("Trinket")) {
              return false;
          }
          String name = dto.getName().toLowerCase();
          return !name.startsWith("doran");
      })
      .collect(Collectors.toList());


    // Gets the ids of the top 7 used items on the champ
    List<Integer> top7Ids = filtered.stream()
      .sorted(Comparator.comparingDouble(ChampionItemWinrate::getWinRate)
                        .reversed())
      .limit(7)
      .map(ChampionItemWinrate::getItemId)
      .toList();
    
    // If there aren't enough items, fetch more
    if (top7Ids.size() < 7) {
      List<ChampionItemWinrate> fallbackAll = 
        winRepo.findByChampionNameIgnoreCase(champion).stream()
          .filter(r -> {
            ItemDto dto = itemService.getItem(r.getItemId());
            return dto != null
                && !dto.getTags().contains("Consumable")
                && !dto.getTags().contains("Trinket")
                && !dto.getName().toLowerCase().startsWith("doran");
          })
          .toList();
    
      // Add as many as you need to reach 7, skipping duplicates
      for (ChampionItemWinrate r : fallbackAll) {
        if (top7Ids.size() >= 7) break;
        if (!top7Ids.contains(r.getItemId())) {
          top7Ids.add(r.getItemId());
        }
      }
      }

    return top7Ids.stream()
      .map(itemService::getItem)
      .filter(Objects::nonNull)
      .toList();
  }
}
