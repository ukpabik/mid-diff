package com.main.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.main.server.stats.ChampionItemWinrate;

/**
 * Repository for querying per-champion item winrate records.
 */
public interface ChampionItemWinrateRepo extends JpaRepository<ChampionItemWinrate, Long> {
  
  /**
   * Finds all winrate entries for a given champion name, ignoring case.
   *
   * @param championName the name of the champion to search for
   * @return list of ChampionItemWinrate entries matching the champion name
   */
  @Query("SELECT c FROM ChampionItemWinrate c WHERE LOWER(c.championName) = LOWER(:championName)")
  List<ChampionItemWinrate> findByChampionNameIgnoreCase(@Param("championName") String championName);
}
