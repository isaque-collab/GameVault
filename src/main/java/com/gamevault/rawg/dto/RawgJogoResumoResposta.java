package com.gamevault.rawg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgJogoResumoResposta (
        Long id,

        @JsonProperty("name")
        String nome,

        @JsonProperty("released")
        LocalDate dataLancamento,

        @JsonProperty("background_image")
        String imagemFundo,

        @JsonProperty("rating")
        Double notaRawg,

        @JsonProperty("ratings_count")
        Integer totalAvaliacoes,

        Integer metacritic
) {
}
