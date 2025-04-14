package com.main.server.service;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.main.server.model.Match;
import com.main.server.repository.MatchRepository;

@Service
public class CsvService {
  @Autowired
  private MatchRepository matchRepository;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private volatile File latestCsv = null;

  public void generateCsvAsync(){
    executor.submit(() -> {
      try {
        List<Match> matches = matchRepository.findAll();
        File tempFile = File.createTempFile("match_dump_", ".csv");
        try (PrintWriter writer = new PrintWriter(tempFile)) {
          writer.println("puuid,matchId,championName,teamPosition,win,kills,deaths,assists,goldEarned,goldSpent,csPerMin,kda,visionScore,wardsPlaced,wardsKilled,damageDealtToChampions,totalDamageTaken,gameMode,queueId,gameDuration,totalMinionsKilled,neutralMinionsKilled,turretTakedowns,inhibitorTakedowns,gameStartTimestamp");
          for (Match m : matches) {
            writer.println(String.join(",", List.of(
              m.getPuuid(),
              m.getMatchId(),
              m.getChampionName(),
              m.getTeamPosition(),
              String.valueOf(m.isWin()),
              String.valueOf(m.getKills()),
              String.valueOf(m.getDeaths()),
              String.valueOf(m.getAssists()),
              String.valueOf(m.getGoldEarned()),
              String.valueOf(m.getGoldSpent()),
              String.valueOf(m.getCsPerMin()),
              String.valueOf(m.getKda()),
              String.valueOf(m.getVisionScore()),
              String.valueOf(m.getWardsPlaced()),
              String.valueOf(m.getWardsKilled()),
              String.valueOf(m.getDamageDealtToChampions()),
              String.valueOf(m.getTotalDamageTaken()),
              m.getGameMode(),
              String.valueOf(m.getQueueId()),
              String.valueOf(m.getGameDuration()),
              String.valueOf(m.getTotalMinionsKilled()),
              String.valueOf(m.getNeutralMinionsKilled()),
              String.valueOf(m.getTurretTakedowns()),
              String.valueOf(m.getInhibitorTakedowns()),
              String.valueOf(m.getGameStartTimestamp())
            )));
          }
        }
        latestCsv = tempFile;
        System.out.println("CSV export completed: " + latestCsv.getAbsolutePath());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  public File getLatestCsv(){
    return latestCsv;
  }
}
