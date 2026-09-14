package com.gamevault.jogo.dto;

import java.time.LocalDate;

public record JogoResumoResposta(
        Long rawgGameId,
        String nome,
        LocalDate dataLancamento,
        String imagemFundo,
        Double notaRawg,
        Integer quantidadeAvaliacoesRawg,
        Integer metacritic
) {
}
