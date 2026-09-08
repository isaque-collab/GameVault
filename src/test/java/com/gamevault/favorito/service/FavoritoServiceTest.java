package com.gamevault.favorito.service;


import com.gamevault.favorito.entity.Favorito;
import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.favorito.repository.FavoritoRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private FavoritoService favoritoService;

    @BeforeEach
    void configurar() {
        favoritoService = new FavoritoService(
                favoritoRepository,
                usuarioRepository
        );
    }

    @Test
    void deveAdicionarFavoritoQuandoUsuarioExisteEJogoAindaNaoFoiFavoritado() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        Usuario usuario = new Usuario();

        when(
                favoritoRepository.existsByUsuarioIdAndRawgGameId(
                        userId,
                        rawgGameId
                )
        ).thenReturn(false);

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.of(usuario));

        when(favoritoRepository.save(any(Favorito.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Favorito favorito = favoritoService.adicionarFavorito(
                userId,
                rawgGameId
        );

        assertAll(
                () -> assertSame(usuario, favorito.getUseario()),
                () -> assertEquals(rawgGameId, favorito.getRawgGameId())
        );

        verify(favoritoRepository).save(any(Favorito.class));
    }

    @Test
    void deveImpedirAdicionarJogoQueJaEstaNosFavoritos() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        when(
                favoritoRepository.existsByUsuarioIdAndRawgGameId(
                        userId,
                        rawgGameId
                )
        ).thenReturn(true);

        assertThrows(
                FavoritoJaExisteException.class,
                () -> favoritoService.adicionarFavorito(userId, rawgGameId)
        );

        verifyNoInteractions(usuarioRepository);
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void deveFalharAoAdicionarFavoritoParaUsuarioInexistente() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        when(
                favoritoRepository.existsByUsuarioIdAndRawgGameId(
                        userId,
                        rawgGameId
                )
        ).thenReturn(false);

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> favoritoService.adicionarFavorito(userId, rawgGameId)
        );

        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void deveRemoverFavoritoExistente() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        Favorito favorito = new Favorito();

        when(
                favoritoRepository.findByUsuarioIdAndRawgGameId(
                        userId,
                        rawgGameId
                )
        ).thenReturn(Optional.of(favorito));

        favoritoService.removeFavorito(userId, rawgGameId);

        verify(favoritoRepository).delete(favorito);
    }

    @Test
    void deveFalharAoRemoverJogoQueNaoEstaNosFavoritos() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        when(
                favoritoRepository.findByUsuarioIdAndRawgGameId(
                        userId,
                        rawgGameId
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                FavoritoNaoEncontradoException.class,
                () -> favoritoService.removeFavorito(userId, rawgGameId)
        );

        verify(favoritoRepository, never()).delete(any());
    }

    @Test
    void deveListarFavoritosDoUsuario() {
        Long userId = 1L;

        Favorito primeiroFavorito = new Favorito();
        primeiroFavorito.setRawgGameId(3498L);

        Favorito segundoFavorito = new Favorito();
        segundoFavorito.setRawgGameId(3328L);

        when(favoritoRepository.findAllByUsuarioId(userId))
                .thenReturn(List.of(primeiroFavorito, segundoFavorito));

        List<Favorito> favoritos = favoritoService.listFavorites(userId);

        assertAll(
                () -> assertEquals(2, favoritos.size()),
                () -> assertEquals(
                        3498L,
                        favoritos.get(0).getRawgGameId()
                ),
                () -> assertEquals(
                        3328L,
                        favoritos.get(1).getRawgGameId()
                )
        );
    }
}
