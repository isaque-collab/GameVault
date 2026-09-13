package com.gamevault.rawg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rawg")
public record RawgProperties(
        String baseUrl,
        String apiKey
) {
}
