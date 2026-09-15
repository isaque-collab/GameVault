package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.BuscaJogosResposta;
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

    public BuscaJogosResposta buscarJogosPorNome(
            String nome,
            int pagina,
            String genero
    ) {
        RawgBuscaJogosResposta respostaRawg =
                rawgClient.buscarJogosPorNome(
                        nome,
                        pagina,
                        genero
                );

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
