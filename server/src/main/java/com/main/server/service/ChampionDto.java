package com.main.server.service;

import java.util.List;

public class ChampionDto {
  private final int id;
  private final String name;
  private final List<String> tags;

  public ChampionDto(int id, String name, List<String> tags) {
    this.id = id;
    this.name = name;
    this.tags = tags;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<String> getTags() {
    return tags;
  }
}
