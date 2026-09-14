package com.gamevault.rawg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgBuscaJogosResposta(
        @JsonProperty("count")
        Integer total,

        @JsonProperty("results")
        List<RawgJogoResumoResposta> resultados
) {
}
