package com.gamevault.jogo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ItemColecaoJogoResposta(

        Long id,
        Long rawgGameId,
        LocalDateTime criadoEm,

        String nome,
        LocalDate dataLancamento,
        String imagemFundo,
        Double notaRawg,
        Integer metacritic,

        boolean metadadosDisponiveis

) {
}
