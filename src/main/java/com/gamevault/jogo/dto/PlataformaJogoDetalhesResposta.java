package com.gamevault.jogo.dto;

public record PlataformaJogoDetalhesResposta(
        Long id,
        String nome,
        String slug,
        String requisitoMinimo,
        String requisitoRecomendado
) {
}
