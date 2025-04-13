package com.main.server.config;

import io.github.cdimascio.dotenv.Dotenv;

public class RiotConfig {
    private final Dotenv dotenv = Dotenv.load();

    public String getKey() {
        return dotenv.get("RIOT_API_KEY");
    }
}