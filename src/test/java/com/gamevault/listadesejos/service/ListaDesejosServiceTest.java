package com.gamevault.listadesejos.service;

import com.gamevault.listadesejos.entity.ItemListaDesejos;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.listadesejos.repository.ItemListaDesejosRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListaDesejosServiceTest {

    @Mock
    private ItemListaDesejosRepository itemListaDesejosRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private ListaDesejosService listaDesejosService;

    @BeforeEach
    void configurar() {
        listaDesejosService = new ListaDesejosService(
                itemListaDesejosRepository,
                usuarioRepository
        );
    }

    @Test
    void deveAdicionarJogoNaListaDeDesejosQuandoUsuarioExisteEJogoAindaNaoFoiAdicionado() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        Usuario usuario = new Usuario();

        when(
                itemListaDesejosRepository
                        .existsByUsuarioIdAndRawgGameId(
                                userId,
                                rawgGameId
                        )
        ).thenReturn(false);

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.of(usuario));

        when(
                itemListaDesejosRepository
                        .save(any(ItemListaDesejos.class))
        ).thenAnswer(invocation -> invocation.getArgument(0));

        ItemListaDesejos item = listaDesejosService.adicionar(
                userId,
                rawgGameId
        );

        assertAll(
                () -> assertSame(usuario, item.getUsuario()),
                () -> assertEquals(
                        rawgGameId,
                        item.getRawgGameId()
                )
        );

        verify(itemListaDesejosRepository)
                .save(any(ItemListaDesejos.class));
    }

    @Test
    void deveImpedirAdicionarJogoQueJaEstaNaListaDeDesejos() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        when(
                itemListaDesejosRepository
                        .existsByUsuarioIdAndRawgGameId(
                                userId,
                                rawgGameId
                        )
        ).thenReturn(true);

        assertThrows(
                ItemListaDesejosJaExisteException.class,
                () -> listaDesejosService.adicionar(
                        userId,
                        rawgGameId
                )
        );

        verifyNoInteractions(usuarioRepository);

        verify(
                itemListaDesejosRepository,
                never()
        ).save(any());
    }

    @Test
    void deveFalharAoAdicionarJogoNaListaDeDesejosParaUsuarioInexistente() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        when(
                itemListaDesejosRepository
                        .existsByUsuarioIdAndRawgGameId(
                                userId,
                                rawgGameId
                        )
        ).thenReturn(false);

        when(usuarioRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> listaDesejosService.adicionar(
                        userId,
                        rawgGameId
                )
        );

        verify(
                itemListaDesejosRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRemoverJogoExistenteDaListaDeDesejos() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        ItemListaDesejos item = new ItemListaDesejos();

        when(
                itemListaDesejosRepository
                        .findByUsuarioIdAndRawgGameId(
                                userId,
                                rawgGameId
                        )
        ).thenReturn(Optional.of(item));

        listaDesejosService.remover(
                userId,
                rawgGameId
        );

        verify(itemListaDesejosRepository).delete(item);
    }

    @Test
    void deveFalharAoRemoverJogoQueNaoEstaNaListaDeDesejos() {
        Long userId = 1L;
        Long rawgGameId = 3498L;

        when(
                itemListaDesejosRepository
                        .findByUsuarioIdAndRawgGameId(
                                userId,
                                rawgGameId
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                ItemListaDesejosNaoEncontradoException.class,
                () -> listaDesejosService.remover(
                        userId,
                        rawgGameId
                )
        );

        verify(
                itemListaDesejosRepository,
                never()
        ).delete(any());
    }

    @Test
    void deveListarJogosDaListaDeDesejosDoUsuario() {
        Long userId = 1L;

        ItemListaDesejos primeiroItem = new ItemListaDesejos();
        primeiroItem.setRawgGameId(3498L);

        ItemListaDesejos segundoItem = new ItemListaDesejos();
        segundoItem.setRawgGameId(3328L);

        when(
                itemListaDesejosRepository.findAllByUsuarioId(userId)
        ).thenReturn(
                List.of(
                        primeiroItem,
                        segundoItem
                )
        );

        List<ItemListaDesejos> itens =
                listaDesejosService.listar(userId);

        assertAll(
                () -> assertEquals(2, itens.size()),
                () -> assertEquals(
                        3498L,
                        itens.get(0).getRawgGameId()
                ),
                () -> assertEquals(
                        3328L,
                        itens.get(1).getRawgGameId()
                )
        );
    }
}