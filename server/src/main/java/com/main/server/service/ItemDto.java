package com.main.server.service;

public class ItemDto {
  private final int id;
  private final String name;
  private final String description;
  private final int totalGold;
  private final String imageFilename;

  public ItemDto(int id, String name, String description, int totalGold, String imageFilename) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.totalGold = totalGold;
    this.imageFilename = imageFilename;
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
}
