package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.jogo.dto.JogoResumoResposta;
import com.gamevault.jogo.dto.OrdenacaoJogo;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoResumoResposta;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class JogoService {

    private static final long DIAS_LANCAMENTOS_RECENTES = 30;

    private final RawgClient rawgClient;
    private final Clock clock;

    public JogoService(
            RawgClient rawgClient,
            Clock clock
    ) {
        this.rawgClient = rawgClient;
        this.clock = clock;
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

    public BuscaJogosResposta buscarJogosPopulares(
            int pagina
    ) {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        OrdenacaoJogo.POPULARIDADE,
                        pagina
                );

        return buscarJogos(filtro);
    }

    public BuscaJogosResposta buscarLancamentosRecentes(
            int pagina
    ) {
        LocalDate hoje = LocalDate.now(clock);

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        hoje.minusDays(
                                DIAS_LANCAMENTOS_RECENTES - 1
                        ),
                        hoje,
                        OrdenacaoJogo.LANCAMENTO,
                        pagina
                );

        return buscarJogos(filtro);
    }

    public BuscaJogosResposta buscarJogosMaisBemAvaliados(
            int pagina
    ) {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        OrdenacaoJogo.AVALIACAO_RAWG,
                        pagina
                );

        return buscarJogos(filtro);
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
