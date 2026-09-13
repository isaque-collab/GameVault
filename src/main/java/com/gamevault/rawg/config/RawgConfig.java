package com.gamevault.rawg.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RawgProperties.class)
public class RawgConfig {

    @Bean
    public RestClient rawgRestClient(
            RestClient.Builder builder,
            RawgProperties properties
    ) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}
