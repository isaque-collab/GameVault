package com.gamevault.listadesejos.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarItemListaDesejosRequisicao(

        @NotNull(message = "O ID do jogo é obrigatório")
        Long rawgGameId
) {
}
