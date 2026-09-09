package com.gamevault.avaliacao.service;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.exception.NotaAvaliacaoInvalidaException;
import com.gamevault.avaliacao.repository.AvaliacaoRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void configurar() {
        avaliacaoService = new AvaliacaoService(
                avaliacaoRepository,
                usuarioRepository
        );
    }

    @Test
    void deveCriarAvaliacaoQuandoUsuarioAindaNaoAvaliouOJogo() {
        Long usuarioId = 1L;
        Long rawgGameId = 3498L;
        Byte nota = 5;

        Usuario usuario = new Usuario();

        when(
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        ).thenReturn(Optional.empty());

        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Avaliacao avaliacao = avaliacaoService.avaliar(
                usuarioId,
                rawgGameId,
                nota
        );

        assertAll(
                () -> assertSame(usuario, avaliacao.getUsuario()),
                () -> assertEquals(rawgGameId, avaliacao.getRawgGameId()),
                () -> assertEquals(nota, avaliacao.getRating())
        );

        verify(avaliacaoRepository).save(any(Avaliacao.class));
    }

    @Test
    void deveAtualizarNotaQuandoUsuarioJaAvaliouOJogo() {
        Long usuarioId = 1L;
        Long rawgGameId = 3498L;

        Avaliacao avaliacaoExistente = new Avaliacao();
        avaliacaoExistente.setRawgGameId(rawgGameId);
        avaliacaoExistente.setRating((byte) 3);

        when(
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        ).thenReturn(Optional.of(avaliacaoExistente));

        Avaliacao resultado = avaliacaoService.avaliar(
                usuarioId,
                rawgGameId,
                (byte) 5
        );

        assertAll(
                () -> assertSame(avaliacaoExistente, resultado),
                () -> assertEquals((byte) 5, resultado.getRating())
        );

        verifyNoInteractions(usuarioRepository);

        verify(
                avaliacaoRepository,
                never()
        ).save(any());
    }

    @Test
    void deveFalharAoAvaliarQuandoUsuarioNaoExiste() {
        Long usuarioId = 1L;
        Long rawgGameId = 3498L;

        when(
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        ).thenReturn(Optional.empty());

        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> avaliacaoService.avaliar(
                        usuarioId,
                        rawgGameId,
                        (byte) 5
                )
        );

        verify(
                avaliacaoRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarNotaAbaixoDoMinimo() {
        assertThrows(
                NotaAvaliacaoInvalidaException.class,
                () -> avaliacaoService.avaliar(
                        1L,
                        3498L,
                        (byte) 0
                )
        );

        verifyNoInteractions(
                avaliacaoRepository,
                usuarioRepository
        );
    }

    @Test
    void deveRejeitarNotaAcimaDoMaximo() {
        assertThrows(
                NotaAvaliacaoInvalidaException.class,
                () -> avaliacaoService.avaliar(
                        1L,
                        3498L,
                        (byte) 6
                )
        );

        verifyNoInteractions(
                avaliacaoRepository,
                usuarioRepository
        );
    }

    @Test
    void deveRejeitarNotaNula() {
        assertThrows(
                NotaAvaliacaoInvalidaException.class,
                () -> avaliacaoService.avaliar(
                        1L,
                        3498L,
                        null
                )
        );

        verifyNoInteractions(
                avaliacaoRepository,
                usuarioRepository
        );
    }

    @Test
    void deveRemoverAvaliacaoExistente() {
        Long usuarioId = 1L;
        Long rawgGameId = 3498L;

        Avaliacao avaliacao = new Avaliacao();

        when(
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        ).thenReturn(Optional.of(avaliacao));

        avaliacaoService.remover(
                usuarioId,
                rawgGameId
        );

        verify(avaliacaoRepository).delete(avaliacao);
    }

    @Test
    void deveFalharAoRemoverAvaliacaoInexistente() {
        Long usuarioId = 1L;
        Long rawgGameId = 3498L;

        when(
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                AvaliacaoNaoEncontradaException.class,
                () -> avaliacaoService.remover(
                        usuarioId,
                        rawgGameId
                )
        );

        verify(
                avaliacaoRepository,
                never()
        ).delete(any());
    }

    @Test
    void deveBuscarAvaliacaoDoUsuarioParaOJogo() {
        Long usuarioId = 1L;
        Long rawgGameId = 3498L;

        Avaliacao avaliacao = new Avaliacao();

        when(
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        ).thenReturn(Optional.of(avaliacao));

        Optional<Avaliacao> resultado =
                avaliacaoService.buscarAvaliacaoDoUsuario(
                        usuarioId,
                        rawgGameId
                );

        assertTrue(resultado.isPresent());
        assertSame(avaliacao, resultado.get());
    }

    @Test
    void deveCalcularMediaDasAvaliacoesDoJogo() {
        Long rawgGameId = 3498L;

        when(avaliacaoRepository.calcularMediaPorJogo(rawgGameId))
                .thenReturn(Optional.of(4.5));

        Optional<Double> media =
                avaliacaoService.calcularMediaPorJogo(rawgGameId);

        assertTrue(media.isPresent());
        assertEquals(4.5, media.get());
    }

    @Test
    void deveContarAvaliacoesDoJogo() {
        Long rawgGameId = 3498L;

        when(avaliacaoRepository.countByRawgGameId(rawgGameId))
                .thenReturn(12L);

        long quantidade =
                avaliacaoService.contarAvaliacoes(rawgGameId);

        assertEquals(12L, quantidade);
    }
}