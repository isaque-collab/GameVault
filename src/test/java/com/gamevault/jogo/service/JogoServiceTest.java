package com.gamevault.jogo.service;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.service.AvaliacaoService;
import com.gamevault.favorito.service.FavoritoService;
import com.gamevault.jogo.dto.*;
import com.gamevault.listadesejos.service.ListaDesejosService;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

    @Mock
    private RawgClient rawgClient;

    @Mock
    private AvaliacaoService avaliacaoService;

    @Mock
    private FavoritoService favoritoService;

    @Mock
    private ListaDesejosService listaDesejosService;

    private JogoService jogoService;

    @BeforeEach
    void configurar() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-16T12:00:00Z"),
                ZoneOffset.UTC
        );

        jogoService = new JogoService(
                rawgClient,
                avaliacaoService,
                favoritoService,
                listaDesejosService,
                clock
        );
    }

    @Test
    void deveBuscarCatalogoComFiltrosEMapearRespostaDaRawg() {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        "Portal",
                        "action",
                        4,
                        "valve",
                        "electronic-arts",
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        OrdenacaoJogo.METACRITIC,
                        2
                );

        RawgJogoResumoResposta jogoRawg =
                new RawgJogoResumoResposta(
                        4200L,
                        "Portal 2",
                        LocalDate.of(2011, 4, 18),
                        "https://exemplo.com/portal-2.jpg",
                        4.61,
                        6900,
                        95
                );

        RawgBuscaJogosResposta respostaRawg =
                new RawgBuscaJogosResposta(
                        41,
                        List.of(jogoRawg)
                );

        when(rawgClient.buscarJogos(filtro))
                .thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarJogos(filtro);

        JogoResumoResposta jogo =
                resultado.jogos().get(0);

        assertAll(
                () -> assertEquals(2, resultado.pagina()),
                () -> assertEquals(
                        41,
                        resultado.totalResultados()
                ),
                () -> assertEquals(1, resultado.jogos().size()),
                () -> assertEquals(
                        4200L,
                        jogo.rawgGameId()
                ),
                () -> assertEquals(
                        "Portal 2",
                        jogo.nome()
                ),
                () -> assertEquals(
                        LocalDate.of(2011, 4, 18),
                        jogo.dataLancamento()
                ),
                () -> assertEquals(
                        "https://exemplo.com/portal-2.jpg",
                        jogo.imagemFundo()
                ),
                () -> assertEquals(
                        4.61,
                        jogo.notaRawg()
                ),
                () -> assertEquals(
                        6900,
                        jogo.quantidadeAvaliacoesRawg()
                ),
                () -> assertEquals(
                        95,
                        jogo.metacritic()
                )
        );

        verify(rawgClient).buscarJogos(filtro);
    }

    @Test
    void deveRetornarListaVaziaAoBuscarCatalogoSemResultados() {
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
                        1
                );

        RawgBuscaJogosResposta respostaRawg =
                new RawgBuscaJogosResposta(
                        0,
                        List.of()
                );

        when(rawgClient.buscarJogos(filtro))
                .thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarJogos(filtro);

        assertAll(
                () -> assertEquals(1, resultado.pagina()),
                () -> assertEquals(
                        0,
                        resultado.totalResultados()
                ),
                () -> assertTrue(
                        resultado.jogos().isEmpty()
                )
        );

        verify(rawgClient).buscarJogos(filtro);
    }

    @Test
    void deveBuscarJogosPopulares() {
        FiltroCatalogoJogos filtroEsperado =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        OrdenacaoJogo.POPULARIDADE,
                        1
                );

        RawgBuscaJogosResposta respostaRawg =
                new RawgBuscaJogosResposta(
                        0,
                        List.of()
                );

        when(rawgClient.buscarJogos(filtroEsperado))
                .thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarJogosPopulares(1);

        assertAll(
                () -> assertEquals(1, resultado.pagina()),
                () -> assertEquals(
                        0,
                        resultado.totalResultados()
                ),
                () -> assertTrue(resultado.jogos().isEmpty())
        );

        verify(rawgClient).buscarJogos(filtroEsperado);
    }

    @Test
    void deveBuscarLancamentosDosUltimosTrintaDias() {
        FiltroCatalogoJogos filtroEsperado =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 8, 18),
                        LocalDate.of(2026, 9, 16),
                        OrdenacaoJogo.LANCAMENTO,
                        1
                );

        RawgBuscaJogosResposta respostaRawg =
                new RawgBuscaJogosResposta(
                        0,
                        List.of()
                );

        when(rawgClient.buscarJogos(filtroEsperado))
                .thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarLancamentosRecentes(1);

        assertAll(
                () -> assertEquals(1, resultado.pagina()),
                () -> assertEquals(
                        0,
                        resultado.totalResultados()
                ),
                () -> assertTrue(resultado.jogos().isEmpty())
        );

        verify(rawgClient).buscarJogos(filtroEsperado);
    }

    @Test
    void deveBuscarJogosMaisBemAvaliados() {
        FiltroCatalogoJogos filtroEsperado =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        OrdenacaoJogo.AVALIACAO_RAWG,
                        1
                );

        RawgBuscaJogosResposta respostaRawg =
                new RawgBuscaJogosResposta(
                        0,
                        List.of()
                );

        when(rawgClient.buscarJogos(filtroEsperado))
                .thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarJogosMaisBemAvaliados(1);

        assertAll(
                () -> assertEquals(1, resultado.pagina()),
                () -> assertEquals(
                        0,
                        resultado.totalResultados()
                ),
                () -> assertTrue(resultado.jogos().isEmpty())
        );

        verify(rawgClient).buscarJogos(filtroEsperado);
    }

    @Test
    void deveBuscarDetalhesDoJogoECombinarDadosDaRawg() {
        RawgJogoDetalhesResposta detalhesRawg =
                new RawgJogoDetalhesResposta(
                        3498L,
                        "Grand Theft Auto V",
                        "Um jogo de ação em mundo aberto.",
                        LocalDate.of(2013, 9, 17),
                        "https://exemplo.com/gta-v.jpg",
                        4.47,
                        7000,
                        92,
                        32,
                        new RawgReferenciaResposta(
                                17L,
                                "Mature",
                                "mature"
                        ),
                        List.of(
                                new RawgReferenciaResposta(
                                        4L,
                                        "Action",
                                        "action"
                                )
                        ),
                        List.of(
                                new RawgPlataformaJogoResposta(
                                        new RawgReferenciaResposta(
                                                4L,
                                                "PC",
                                                "pc"
                                        ),
                                        new RawgRequisitosResposta(
                                                "Windows 10",
                                                "Windows 11"
                                        )
                                )
                        ),
                        List.of(
                                new RawgReferenciaResposta(
                                        3524L,
                                        "Rockstar North",
                                        "rockstar-north"
                                )
                        ),
                        List.of(
                                new RawgReferenciaResposta(
                                        2155L,
                                        "Rockstar Games",
                                        "rockstar-games"
                                )
                        )
                );

        RawgScreenshotsResposta screenshotsRawg =
                new RawgScreenshotsResposta(
                        2,
                        List.of(
                                new RawgScreenshotResposta(
                                        "https://exemplo.com/screenshot-1.jpg",
                                        false
                                ),
                                new RawgScreenshotResposta(
                                        "https://exemplo.com/screenshot-2.jpg",
                                        true
                                )
                        )
                );

        when(rawgClient.buscarJogoPorId(3498L))
                .thenReturn(detalhesRawg);

        when(rawgClient.buscarScreenshotsPorJogo(3498L))
                .thenReturn(screenshotsRawg);

        when(
                avaliacaoService.calcularMediaPorJogo(3498L)
        ).thenReturn(
                Optional.of(4.5)
        );

        when(
                avaliacaoService.contarAvaliacoes(3498L)
        ).thenReturn(12L);

        JogoDetalhesResposta resultado =
                jogoService.buscarDetalhesJogo(
                        3498L,
                        null
                );

        assertAll(
                () -> assertEquals(
                        3498L,
                        resultado.rawgGameId()
                ),
                () -> assertEquals(
                        "Grand Theft Auto V",
                        resultado.nome()
                ),
                () -> assertEquals(
                        "Um jogo de ação em mundo aberto.",
                        resultado.descricao()
                ),
                () -> assertEquals(
                        32,
                        resultado.tempoMedioJogo()
                ),
                () -> assertEquals(
                        "Mature",
                        resultado.classificacaoEtaria()
                ),
                () -> assertEquals(
                        List.of("Action"),
                        resultado.generos()
                ),
                () -> assertEquals(
                        "PC",
                        resultado.plataformas()
                                .get(0)
                                .nome()
                ),
                () -> assertEquals(
                        "Windows 10",
                        resultado.plataformas()
                                .get(0)
                                .requisitoMinimo()
                ),
                () -> assertEquals(
                        List.of("Rockstar North"),
                        resultado.desenvolvedoras()
                ),
                () -> assertEquals(
                        List.of("Rockstar Games"),
                        resultado.publicadoras()
                ),
                () -> assertEquals(
                        List.of(
                                "https://exemplo.com/screenshot-1.jpg"
                        ),
                        resultado.screenshots()
                ),
                () -> assertEquals(
                        4.5,
                        resultado.mediaAvaliacoesGameVault()
                ),
                () -> assertEquals(
                        12L,
                        resultado.quantidadeAvaliacoesGameVault()
                ),
                () -> assertNull(resultado.minhaAvaliacao()),
                () -> assertNull(resultado.favoritado()),
                () -> assertNull(resultado.naListaDesejos())
        );

        verify(rawgClient).buscarJogoPorId(3498L);
        verify(rawgClient).buscarScreenshotsPorJogo(3498L);
        verify(avaliacaoService)
                .calcularMediaPorJogo(3498L);

        verify(avaliacaoService)
                .contarAvaliacoes(3498L);

        verify(
                avaliacaoService,
                never()
        ).buscarAvaliacaoDoUsuario(
                anyLong(),
                anyLong()
        );

        verifyNoInteractions(
                favoritoService,
                listaDesejosService
        );
    }

    @Test
    void deveRetornarResumoGameVaultVazioQuandoJogoNaoPossuirAvaliacoes() {
        RawgJogoDetalhesResposta detalhesRawg =
                new RawgJogoDetalhesResposta(
                        3498L,
                        "Grand Theft Auto V",
                        "Descrição",
                        LocalDate.of(2013, 9, 17),
                        "imagem.jpg",
                        4.47,
                        7000,
                        92,
                        32,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()
                );

        RawgScreenshotsResposta screenshotsRawg =
                new RawgScreenshotsResposta(
                        0,
                        List.of()
                );

        when(rawgClient.buscarJogoPorId(3498L))
                .thenReturn(detalhesRawg);

        when(rawgClient.buscarScreenshotsPorJogo(3498L))
                .thenReturn(screenshotsRawg);

        when(
                avaliacaoService.calcularMediaPorJogo(3498L)
        ).thenReturn(Optional.empty());

        when(
                avaliacaoService.contarAvaliacoes(3498L)
        ).thenReturn(0L);

        JogoDetalhesResposta resultado =
                jogoService.buscarDetalhesJogo(
                        3498L,
                        null);

        assertAll(
                () -> assertEquals(
                        null,
                        resultado.mediaAvaliacoesGameVault()
                ),
                () -> assertEquals(
                        0L,
                        resultado.quantidadeAvaliacoesGameVault()
                )
        );

        verify(avaliacaoService)
                .calcularMediaPorJogo(3498L);

        verify(avaliacaoService)
                .contarAvaliacoes(3498L);
    }

    @Test
    void deveRetornarDadosPersonalizadosDoUsuarioAutenticado() {
        RawgJogoDetalhesResposta detalhesRawg =
                new RawgJogoDetalhesResposta(
                        3498L,
                        "Grand Theft Auto V",
                        "Descrição",
                        LocalDate.of(2013, 9, 17),
                        "imagem.jpg",
                        4.47,
                        7000,
                        92,
                        32,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()
                );

        RawgScreenshotsResposta screenshotsRawg =
                new RawgScreenshotsResposta(
                        0,
                        List.of()
                );

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setRating((byte) 5);

        when(rawgClient.buscarJogoPorId(3498L))
                .thenReturn(detalhesRawg);

        when(rawgClient.buscarScreenshotsPorJogo(3498L))
                .thenReturn(screenshotsRawg);

        when(
                avaliacaoService.calcularMediaPorJogo(3498L)
        ).thenReturn(Optional.of(4.5));

        when(
                avaliacaoService.contarAvaliacoes(3498L)
        ).thenReturn(12L);

        when(
                avaliacaoService.buscarAvaliacaoDoUsuario(
                        1L,
                        3498L
                )
        ).thenReturn(Optional.of(avaliacao));

        when(
                favoritoService.estaFavoritado(
                        1L,
                        3498L
                )
        ).thenReturn(true);

        when(
                listaDesejosService.estaNaListaDeDesejos(
                        1L,
                        3498L
                )
        ).thenReturn(false);

        JogoDetalhesResposta resultado =
                jogoService.buscarDetalhesJogo(
                        3498L,
                        1L
                );

        assertAll(
                () -> assertEquals(
                        (byte) 5,
                        resultado.minhaAvaliacao()
                ),
                () -> assertEquals(
                        true,
                        resultado.favoritado()
                ),
                () -> assertEquals(
                        false,
                        resultado.naListaDesejos()
                )
        );

        verify(avaliacaoService)
                .buscarAvaliacaoDoUsuario(
                        1L,
                        3498L
                );

        verify(favoritoService)
                .estaFavoritado(
                        1L,
                        3498L
                );

        verify(listaDesejosService)
                .estaNaListaDeDesejos(
                        1L,
                        3498L
                );
    }
}
