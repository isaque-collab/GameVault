package com.gamevault.jogo.service;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.service.AvaliacaoService;
import com.gamevault.favorito.service.FavoritoService;
import com.gamevault.jogo.dto.*;
import com.gamevault.listadesejos.service.ListaDesejosService;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class JogoService {

    private static final long DIAS_LANCAMENTOS_RECENTES = 30;

    private final RawgClient rawgClient;
    private final AvaliacaoService avaliacaoService;
    private final FavoritoService favoritoService;
    private final ListaDesejosService listaDesejosService;
    private final Clock clock;

    public JogoService(
            RawgClient rawgClient,
            AvaliacaoService avaliacaoService,
            FavoritoService favoritoService,
            ListaDesejosService listaDesejosService,
            Clock clock
    ) {
        this.rawgClient = rawgClient;
        this.avaliacaoService = avaliacaoService;
        this.favoritoService = favoritoService;
        this.listaDesejosService = listaDesejosService;
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

    public JogoDetalhesResposta buscarDetalhesJogo(
            Long rawgGameId,
            Long usuarioId
    ) {
        RawgJogoDetalhesResposta detalhesRawg =
                rawgClient.buscarJogoPorId(rawgGameId);

        RawgScreenshotsResposta screenshotsRawg =
                rawgClient.buscarScreenshotsPorJogo(rawgGameId);

        Double mediaAvaliacoesGameVault =
                avaliacaoService
                        .calcularMediaPorJogo(rawgGameId)
                        .orElse(null);

        long quantidadeAvaliacoesGameVault =
                avaliacaoService.contarAvaliacoes(
                        rawgGameId
                );

        Byte minhaAvaliacao = null;
        Boolean favoritado = null;
        Boolean naListaDesejos = null;

        if (usuarioId != null) {
            minhaAvaliacao =
                    avaliacaoService
                            .buscarAvaliacaoDoUsuario(
                                    usuarioId,
                                    rawgGameId
                            )
                            .map(Avaliacao::getRating)
                            .orElse(null);

            favoritado =
                    favoritoService.estaFavoritado(
                            usuarioId,
                            rawgGameId
                    );

            naListaDesejos =
                    listaDesejosService
                            .estaNaListaDeDesejos(
                                    usuarioId,
                                    rawgGameId
                            );
        }

        return new JogoDetalhesResposta(
                detalhesRawg.id(),
                detalhesRawg.nome(),
                detalhesRawg.descricao(),
                detalhesRawg.dataLancamento(),
                detalhesRawg.imagemFundo(),
                detalhesRawg.notaRawg(),
                detalhesRawg.totalAvaliacoes(),
                detalhesRawg.metacritic(),
                mediaAvaliacoesGameVault,
                quantidadeAvaliacoesGameVault,
                minhaAvaliacao,
                favoritado,
                naListaDesejos,
                detalhesRawg.tempoMedioJogo(),
                mapearClassificacaoEtaria(
                        detalhesRawg.classificacaoEtaria()
                ),
                mapearReferencias(
                        detalhesRawg.generos()
                ),
                mapearPlataformas(
                        detalhesRawg.plataformas()
                ),
                mapearReferencias(
                        detalhesRawg.desenvolvedoras()
                ),
                mapearReferencias(
                        detalhesRawg.publicadoras()
                ),
                screenshotsRawg.resultados()
                        .stream()
                        .filter(screenshot -> !screenshot.oculto())
                        .map(screenshot -> screenshot.image())
                        .toList()
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

    private String mapearClassificacaoEtaria(
            RawgReferenciaResposta classificacaoEtaria
    ) {
        if (classificacaoEtaria == null) {
            return null;
        }

        return classificacaoEtaria.nome();
    }

    private List<String> mapearReferencias(
            List<RawgReferenciaResposta> referencias
    ) {
        if (referencias == null) {
            return Collections.emptyList();
        }

        return referencias
                .stream()
                .map(RawgReferenciaResposta::nome)
                .toList();
    }

    private List<PlataformaJogoDetalhesResposta> mapearPlataformas(
            List<RawgPlataformaJogoResposta> plataformas
    ) {
        if (plataformas == null) {
            return Collections.emptyList();
        }

        return plataformas
                .stream()
                .map(this::mapearPlataforma)
                .toList();
    }

    private PlataformaJogoDetalhesResposta mapearPlataforma(
            RawgPlataformaJogoResposta plataformaRawg
    ) {
        String requisitoMinimo = null;
        String requisitoRecomendado = null;

        if (plataformaRawg.requisitos() != null) {
            requisitoMinimo =
                    plataformaRawg.requisitos().minimo();

            requisitoRecomendado =
                    plataformaRawg.requisitos().recomendado();
        }

        return new PlataformaJogoDetalhesResposta(
                plataformaRawg.plataforma().id(),
                plataformaRawg.plataforma().nome(),
                plataformaRawg.plataforma().slug(),
                requisitoMinimo,
                requisitoRecomendado
        );
    }
}
