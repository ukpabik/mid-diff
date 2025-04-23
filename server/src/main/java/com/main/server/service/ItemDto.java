package com.main.server.service;

import java.util.List;

public class ItemDto {
  private final int id;
  private final String name;
  private final String description;
  private final int totalGold;
  private final String imageFilename;
  private final List<String> tags;
  private final List<Integer> into;

  public ItemDto(int id, String name, String description, int totalGold, String imageFilename, List<String> tags, List<Integer> into) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.totalGold = totalGold;
    this.imageFilename = imageFilename;
    this.tags = List.copyOf(tags);
    this.into = into;
  }
  

  public int getId(){
    return this.id;
  }

  public String getName(){
    return this.name;
  }

  public String getDescription(){
    return this.description;
  }

  public int getTotalGold(){
    return this.totalGold;
  }

  public String getImageFileName(){
    return this.imageFilename;
  }

  public List<String> getTags(){
    return this.tags;
  }
  public List<Integer> getInto() {
    return into;
  }
}
