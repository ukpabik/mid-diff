package com.main.server.service;

import java.util.ArrayList;
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
  private final ItemService itemService;

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

    Integer bestBoot = rows.stream()
      .filter(r -> {
        ItemDto d = itemService.getItem(r.getItemId());
        return d != null && d.getTags().contains("Boots");
      })
      .max(Comparator.comparingDouble(ChampionItemWinrate::getWinRate))
      .map(ChampionItemWinrate::getItemId)
      .orElse(null);

    //  filter to only:
    //    - items with no children (final-tier) AND matching ALLOWED_TAGS
    //    - OR the chosen boots
    //    - OR valid jungle items
    List<ChampionItemWinrate> eligible = rows.stream()
      .filter(r -> {
        ItemDto d = itemService.getItem(r.getItemId());
        if (d == null) return false;
        // drop wards, consumables, trinkets
        if (d.getTags().contains("Consumable") || d.getTags().contains("Trinket")) {
          return false;
        }
        // always allow boots
        if (bestBoot != null && r.getItemId().equals(bestBoot)) {
          return true;
        }
        // allow jungle item
        if (d.getTags().contains("Jungle")) {
          return true;
        }
        if (d.getName().startsWith("Doran")){
          return false;
        }
        // allow final-tier items with allowed tier tag
        boolean isFinalTier = d.getInto().isEmpty();
        return isFinalTier;
      })
      .sorted(Comparator.comparingDouble(ChampionItemWinrate::getWinRate).reversed())
      .collect(Collectors.toList());

    if (bestBoot != null) {
      eligible.removeIf(r -> r.getItemId().equals(bestBoot));
    }

    List<Integer> topOthers = eligible.stream()
      .map(ChampionItemWinrate::getItemId)
      .limit(6)
      .collect(Collectors.toList());

    List<Integer> finalIds = new ArrayList<>(7);
    for (int i = 0; i < topOthers.size(); i++) {
      if (i == 1 && bestBoot != null) {
        finalIds.add(bestBoot);
      }
      finalIds.add(topOthers.get(i));
    }
    if (bestBoot != null && !finalIds.contains(bestBoot)) {
      finalIds.add(1, bestBoot);
    }

    // pad to 7 with any leftover eligible items
    for (ChampionItemWinrate r : eligible) {
      if (finalIds.size() >= 7) break;
      if (!finalIds.contains(r.getItemId())) {
        finalIds.add(r.getItemId());
      }
    }

    return finalIds.stream()
      .map(itemService::getItem)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
}
