package com.gamevault.rawg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgJogoDetalhesResposta(
        Long id,

        @JsonProperty("name")
        String nome,

        @JsonProperty("description_raw")
        String descricao,

        @JsonProperty("released")
        LocalDate dataLancamento,

        @JsonProperty("background_image")
        String imagemFundo,

        @JsonProperty("rating")
        Double notaRawg,

        @JsonProperty("ratings_count")
        Integer totalAvaliacoes,

        Integer metacritic,

        @JsonProperty("playtime")
        Integer tempoMedioJogo,

        @JsonProperty("esrb_rating")
        RawgReferenciaResposta classificacaoEtaria,

        @JsonProperty("genres")
        List<RawgReferenciaResposta> generos,

        @JsonProperty("platforms")
        List<RawgPlataformaJogoResposta> plataformas,

        @JsonProperty("developers")
        List<RawgReferenciaResposta> desenvolvedoras,

        @JsonProperty("publishers")
        List<RawgReferenciaResposta> publicadoras
) {
}
