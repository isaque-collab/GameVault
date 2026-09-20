package com.gamevault.rawg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgReferenciaResposta(
        Long id,

        @JsonProperty("name")
        String nome,

        String slug
) {
}
