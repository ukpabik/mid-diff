package com.main.server.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * JPA entity mapping to the champion_item_winrate table.
 * Uses a composite key defined by ChampionItemWinrateId.
 */
@Entity
@Table(name = "champion_item_winrate")
@IdClass(ChampionItemWinrateId.class)
public class ChampionItemWinrate {

  @Id
  @Column(name = "champion_name", nullable = false)
  private String championName;

  @Id
  @Column(name = "item_id", nullable = false)
  private Integer itemId;

  @Column(name = "win_rate", nullable = false)
  private Double winRate;

  @Column(name = "sample_size", nullable = false)
  private Integer sampleSize;

  public String getChampionName() {
    return championName;
  }

  public void setChampionName(String championName) {
    this.championName = championName;
  }

  public Integer getItemId() {
    return itemId;
  }

  public void setItemId(Integer itemId) {
    this.itemId = itemId;
  }

  public Double getWinRate() {
    return winRate;
  }

  public void setWinRate(Double winRate) {
    this.winRate = winRate;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public void setSampleSize(Integer sampleSize) {
    this.sampleSize = sampleSize;
  }
}