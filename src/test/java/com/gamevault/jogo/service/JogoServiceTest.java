package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.jogo.dto.JogoResumoResposta;
import com.gamevault.jogo.dto.OrdenacaoJogo;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoResumoResposta;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

    @Mock
    private RawgClient rawgClient;

    private JogoService jogoService;

    @BeforeEach
    void configurar() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-09-16T12:00:00Z"),
                ZoneOffset.UTC
        );

        jogoService = new JogoService(
                rawgClient,
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
}
