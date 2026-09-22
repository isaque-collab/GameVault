package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.ItemColecaoJogoResposta;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import com.gamevault.rawg.exception.JogoRawgNaoEncontradoException;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JogoColecaoServiceTest {

    @Mock
    private RawgClient rawgClient;

    private JogoColecaoService jogoColecaoService;

    @BeforeEach
    void configurar() {
        jogoColecaoService =
                new JogoColecaoService(
                        rawgClient
                );
    }

    @Test
    void deveEnriquecerItemComMetadadosDaRawg() {

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        RawgJogoDetalhesResposta jogoRawg =
                new RawgJogoDetalhesResposta(
                        3498L,
                        "Grand Theft Auto V",
                        "Descrição",
                        LocalDate.of(
                                2013,
                                9,
                                17
                        ),
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

        when(
                rawgClient.buscarJogoPorId(3498L)
        ).thenReturn(
                jogoRawg
        );

        ItemColecaoJogoResposta resultado =
                jogoColecaoService.enriquecer(
                        10L,
                        3498L,
                        criadoEm
                );

        assertAll(
                () -> assertEquals(
                        10L,
                        resultado.id()
                ),
                () -> assertEquals(
                        3498L,
                        resultado.rawgGameId()
                ),
                () -> assertEquals(
                        criadoEm,
                        resultado.criadoEm()
                ),
                () -> assertEquals(
                        "Grand Theft Auto V",
                        resultado.nome()
                ),
                () -> assertEquals(
                        LocalDate.of(
                                2013,
                                9,
                                17
                        ),
                        resultado.dataLancamento()
                ),
                () -> assertEquals(
                        "imagem.jpg",
                        resultado.imagemFundo()
                ),
                () -> assertEquals(
                        4.47,
                        resultado.notaRawg()
                ),
                () -> assertEquals(
                        92,
                        resultado.metacritic()
                ),
                () -> assertTrue(
                        resultado.metadadosDisponiveis()
                )
        );

        verify(rawgClient)
                .buscarJogoPorId(3498L);
    }

    @Test
    void deveManterItemQuandoJogoNaoForEncontradoNaRawg() {

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(
                rawgClient.buscarJogoPorId(3498L)
        ).thenThrow(
                new JogoRawgNaoEncontradoException(
                        3498L
                )
        );

        ItemColecaoJogoResposta resultado =
                jogoColecaoService.enriquecer(
                        10L,
                        3498L,
                        criadoEm
                );

        validarItemSemMetadados(
                resultado,
                criadoEm
        );

        verify(rawgClient)
                .buscarJogoPorId(3498L);
    }

    @Test
    void deveManterItemQuandoRawgFalhar() {

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(
                rawgClient.buscarJogoPorId(3498L)
        ).thenThrow(
                new RawgIntegracaoException(
                        "Falha simulada"
                )
        );

        ItemColecaoJogoResposta resultado =
                jogoColecaoService.enriquecer(
                        10L,
                        3498L,
                        criadoEm
                );

        validarItemSemMetadados(
                resultado,
                criadoEm
        );

        verify(rawgClient)
                .buscarJogoPorId(3498L);
    }

    @Test
    void deveManterItemQuandoApiKeyNaoEstiverConfigurada() {

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(
                rawgClient.buscarJogoPorId(3498L)
        ).thenThrow(
                new RawgApiKeyNaoConfiguradaException()
        );

        ItemColecaoJogoResposta resultado =
                jogoColecaoService.enriquecer(
                        10L,
                        3498L,
                        criadoEm
                );

        validarItemSemMetadados(
                resultado,
                criadoEm
        );

        verify(rawgClient)
                .buscarJogoPorId(3498L);
    }

    private void validarItemSemMetadados(
            ItemColecaoJogoResposta resultado,
            LocalDateTime criadoEm
    ) {

        assertAll(
                () -> assertEquals(
                        10L,
                        resultado.id()
                ),
                () -> assertEquals(
                        3498L,
                        resultado.rawgGameId()
                ),
                () -> assertEquals(
                        criadoEm,
                        resultado.criadoEm()
                ),
                () -> assertNull(
                        resultado.nome()
                ),
                () -> assertNull(
                        resultado.dataLancamento()
                ),
                () -> assertNull(
                        resultado.imagemFundo()
                ),
                () -> assertNull(
                        resultado.notaRawg()
                ),
                () -> assertNull(
                        resultado.metacritic()
                ),
                () -> assertFalse(
                        resultado.metadadosDisponiveis()
                )
        );
    }
}