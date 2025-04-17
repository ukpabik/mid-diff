package com.main.server.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerBuild {

  @JsonAlias({ "match_id", "metadata.matchId" })
  @JsonProperty("metadata.matchId")
  private String matchId;

  @JsonProperty("puuid")
  private String puuid;

  @JsonProperty("item0")
  private int item0;

  @JsonProperty("item1")
  private int item1;

  @JsonProperty("item2")
  private int item2;

  @JsonProperty("item3")
  private int item3;

  @JsonProperty("item4")
  private int item4;

  @JsonProperty("item5")
  private int item5;

  @JsonProperty("item6")
  private int item6;

  
  public PlayerBuild() { }

  public PlayerBuild(String matchId, String puuid,
                      int item0, int item1, int item2,
                      int item3, int item4, int item5, int item6) {
    this.matchId = matchId;
    this.puuid   = puuid;
    this.item0   = item0;
    this.item1   = item1;
    this.item2   = item2;
    this.item3   = item3;
    this.item4   = item4;
    this.item5   = item5;
    this.item6   = item6;
  }

  
  public String getMatchId(){
    return this.matchId;
  }
  public void setMatchId(String matchId){
    this.matchId = matchId;
  }

  public String getPuuid(){
    return this.puuid;
  }
  public void setPuuid(String puuid){
    this.puuid = puuid;
  }

  public int getItem0(){
    return this.item0;
  }
  public void setItem0(int item0){
      this.item0 = item0;
  }

  public int getItem1(){
    return this.item1;
  }
  public void setItem1(int item1){
    this.item1 = item1;
  }

  public int getItem2(){
    return this.item2;
  }
  public void setItem2(int item2){
    this.item2 = item2;
  }

  public int getItem3(){
    return this.item3;
  }
  public void setItem3(int item3){
    this.item3 = item3;
  }

  public int getItem4(){
    return this.item4;
  }
  public void setItem4(int item4){
    this.item4 = item4;
  }

  public int getItem5(){
    return this.item5;
  }
  public void setItem5(int item5){
    this.item5 = item5;
  }

  public int getItem6(){
    return this.item6;
  }
  public void setItem6(int item6){
    this.item6 = item6;
  }

  public int[] toArray(){
    return new int[]{ item0, item1, item2, item3, item4, item5, item6 };
  }
}
