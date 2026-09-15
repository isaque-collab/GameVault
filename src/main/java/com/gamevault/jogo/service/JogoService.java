package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.jogo.dto.JogoResumoResposta;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoResumoResposta;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JogoService {

    private final RawgClient rawgClient;

    public JogoService(RawgClient rawgClient) {
        this.rawgClient = rawgClient;
    }

    public BuscaJogosResposta buscarJogos(
            FiltroCatalogoJogos filtro
    ) {
        RawgBuscaJogosResposta respostaRawg =
                rawgClient.buscarJogos(filtro);

        return mapearResposta(
                filtro.pagina(),
                respostaRawg
        );
    }

    private BuscaJogosResposta mapearResposta(
            int pagina,
            RawgBuscaJogosResposta respostaRawg
    ) {
        List<JogoResumoResposta> jogos = respostaRawg
                .resultados()
                .stream()
                .map(this::mapearJogo)
                .toList();

        return new BuscaJogosResposta(
                pagina,
                respostaRawg.total(),
                jogos
        );
    }

    private JogoResumoResposta mapearJogo(
            RawgJogoResumoResposta jogoRawg
    ) {
        return new JogoResumoResposta(
                jogoRawg.id(),
                jogoRawg.nome(),
                jogoRawg.dataLancamento(),
                jogoRawg.imagemFundo(),
                jogoRawg.notaRawg(),
                jogoRawg.totalAvaliacoes(),
                jogoRawg.metacritic()
        );
    }
}
