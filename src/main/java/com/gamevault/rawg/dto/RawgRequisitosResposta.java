package com.gamevault.rawg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgRequisitosResposta(

        @JsonProperty("minimum")
        String minimo,

        @JsonProperty("recommended")
        String recomendado
) {
}
