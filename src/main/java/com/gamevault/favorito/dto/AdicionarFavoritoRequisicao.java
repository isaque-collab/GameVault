package com.gamevault.favorito.dto;

import jakarta.validation.constraints.NotNull;

public record AdicionarFavoritoRequisicao(

        @NotNull(message = "O ID do jogo é obrigatório")
        Long rawgGameId) {
}
