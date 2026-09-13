package com.gamevault.rawg.client;

import com.gamevault.rawg.config.RawgProperties;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RawgClient {

    private final RestClient restClient;
    private final RawgProperties properties;

    public RawgClient(
            @Qualifier("rawgRestClient") RestClient restClient,
            RawgProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public RawgJogoDetalhesResposta buscarJogoPorId(Long rawgGameId) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games/{id}")
                        .queryParam("key", properties.apiKey())
                        .build(rawgGameId))
                .retrieve()
                .body(RawgJogoDetalhesResposta.class);
    }
}
