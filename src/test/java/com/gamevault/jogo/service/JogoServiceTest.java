package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.JogoResumoResposta;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoResumoResposta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
        jogoService = new JogoService(rawgClient);
    }

    @Test
    void deveBuscarJogosPorNomeEMapearRespostaDaRawg() {
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

        when(rawgClient.buscarJogosPorNome("Portal", 2))
                .thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarJogosPorNome("Portal", 2);

        JogoResumoResposta jogo = resultado.jogos().get(0);

        assertAll(
                () -> assertEquals(2, resultado.pagina()),
                () -> assertEquals(
                        41,
                        resultado.totalResultados()
                ),
                () -> assertEquals(1, resultado.jogos().size()),
                () -> assertEquals(4200L, jogo.rawgGameId()),
                () -> assertEquals("Portal 2", jogo.nome()),
                () -> assertEquals(
                        LocalDate.of(2011, 4, 18),
                        jogo.dataLancamento()
                ),
                () -> assertEquals(
                        "https://exemplo.com/portal-2.jpg",
                        jogo.imagemFundo()
                ),
                () -> assertEquals(4.61, jogo.notaRawg()),
                () -> assertEquals(
                        6900,
                        jogo.quantidadeAvaliacoesRawg()
                ),
                () -> assertEquals(95, jogo.metacritic())
        );

        verify(rawgClient)
                .buscarJogosPorNome("Portal", 2);
    }

    @Test
    void deveRetornarListaVaziaQuandoRawgNaoEncontrarJogos() {
        RawgBuscaJogosResposta respostaRawg =
                new RawgBuscaJogosResposta(
                        0,
                        List.of()
                );

        when(rawgClient.buscarJogosPorNome(
                "JogoInexistente",
                1
        )).thenReturn(respostaRawg);

        BuscaJogosResposta resultado =
                jogoService.buscarJogosPorNome(
                        "JogoInexistente",
                        1
                );

        assertAll(
                () -> assertEquals(1, resultado.pagina()),
                () -> assertEquals(
                        0,
                        resultado.totalResultados()
                ),
                () -> assertTrue(resultado.jogos().isEmpty())
        );

        verify(rawgClient).buscarJogosPorNome(
                "JogoInexistente",
                1
        );
    }
}
