package com.gamevault.jogo.dto;

import java.time.LocalDate;
import java.util.List;

public record JogoDetalhesResposta(
        Long rawgGameId,
        String nome,
        String descricao,
        LocalDate dataLancamento,
        String imagemFundo,
        Double notaRawg,
        Integer quantidadeAvaliacoesRawg,
        Integer metacritic,
        Double mediaAvaliacoesGameVault,
        long quantidadeAvaliacoesGameVault,
        Byte minhaAvaliacao,
        Boolean favoritado,
        Boolean naListaDesejos,
        Integer tempoMedioJogo,
        String classificacaoEtaria,
        List<String> generos,
        List<PlataformaJogoDetalhesResposta> plataformas,
        List<String> desenvolvedoras,
        List<String> publicadoras,
        List<String> screenshots
) {

    public JogoDetalhesResposta {
        generos = List.copyOf(generos);
        plataformas = List.copyOf(plataformas);
        desenvolvedoras = List.copyOf(desenvolvedoras);
        publicadoras = List.copyOf(publicadoras);
        screenshots = List.copyOf(screenshots);
    }
}
