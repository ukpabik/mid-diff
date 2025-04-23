package com.main.server.stats;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component that rebuilds the champion_item_winrate table each night.
 */
@Component
public class WinrateCron {

  private final JdbcTemplate jdbc;

  public WinrateCron(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** 
   * Runs every day at 2 AM server time.
   * 1) Truncates the existing winrate table.
   * 2) Executes an insert to repopulate the table with fresh data.
   */
  @Scheduled(cron = "0 0 2 * * *")
  public void rebuildWinrateTable() {
    jdbc.execute("TRUNCATE TABLE champion_item_winrate");
    System.out.println("Rebuilding winrates");
    String cteInsert = """
      WITH all_picks AS (
        SELECT m.champion_name,
               (m.win::INT)     AS win,
               pb.item0         AS item_id
          FROM matches m
          JOIN player_builds pb
            ON m.match_id = pb.match_id
           AND m.puuid     = pb.puuid
        UNION ALL
        SELECT m.champion_name, (m.win::INT), pb.item1
          FROM matches m
          JOIN player_builds pb
            ON m.match_id = pb.match_id
           AND m.puuid     = pb.puuid
        UNION ALL
        -- … repeat for item2 through item6 …
        SELECT m.champion_name, (m.win::INT), pb.item6
          FROM matches m
          JOIN player_builds pb
            ON m.match_id = pb.match_id
           AND m.puuid     = pb.puuid
      ),
      rates AS (
        SELECT champion_name,
               item_id,
               AVG(win)    AS win_rate,
               COUNT(*)    AS sample_size
          FROM all_picks
         WHERE item_id <> 0
         GROUP BY champion_name, item_id
         HAVING COUNT(*) >= 5
      )
      INSERT INTO champion_item_winrate
        (champion_name, item_id, win_rate, sample_size)
      SELECT champion_name, item_id, win_rate, sample_size
        FROM rates;
      """;

    jdbc.execute(cteInsert);
  }
}
