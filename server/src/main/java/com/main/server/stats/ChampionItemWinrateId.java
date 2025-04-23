package com.main.server.stats;


import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for ChampionItemWinrate entity.
 * Consists of championName and itemId fields.
 */
public class ChampionItemWinrateId implements Serializable {
  private String championName;
  private Integer itemId;

  public ChampionItemWinrateId() {}
  public ChampionItemWinrateId(String championName, Integer itemId) {
    this.championName = championName;
    this.itemId = itemId;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ChampionItemWinrateId)) return false;
    ChampionItemWinrateId that = (ChampionItemWinrateId) o;
    return Objects.equals(championName, that.championName)
        && Objects.equals(itemId, that.itemId);
  }

  @Override public int hashCode() {
    return Objects.hash(championName, itemId);
  }
}