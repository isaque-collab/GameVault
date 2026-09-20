package com.gamevault.rawg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgPlataformaJogoResposta(

        @JsonProperty("platform")
        RawgReferenciaResposta plataforma,

        @JsonProperty("requirements")
        RawgRequisitosResposta requisitos
) {
}
