package com.gamevault.jogo.dto;

import java.util.List;

public record BuscaJogosResposta(
        int pagina,
        int totalResultados,
        List<JogoResumoResposta> jogos
) {

    public BuscaJogosResposta {
        jogos = List.copyOf(jogos);
    }
}
