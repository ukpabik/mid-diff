package com.main.server.logs;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MemoryReporter {
  private static final Logger log = LoggerFactory.getLogger(MemoryReporter.class);

  @Scheduled(fixedRate = 60_000)
  public void report() {
    Runtime rt = Runtime.getRuntime();
    long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    long maxMb = rt.maxMemory() / (1024 * 1024);
    long totalMb = rt.totalMemory() / (1024 * 1024);
    long freeMb = rt.freeMemory() / (1024 * 1024);
    log.info("Heap: used={}MB, total={}MB, max={}MB, free={}MB",
              usedMb, totalMb, maxMb, freeMb);
  }
}