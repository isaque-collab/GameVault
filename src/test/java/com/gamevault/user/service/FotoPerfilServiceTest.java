package com.gamevault.user.service;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.FotoPerfilArmazenamentoException;
import com.gamevault.user.exception.FotoPerfilNaoEncontradaException;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import com.gamevault.user.storage.FotoPerfilArquivo;
import com.gamevault.user.storage.FotoPerfilStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FotoPerfilServiceTest {

    private UsuarioRepository usuarioRepository;
    private FotoPerfilStorageService storageService;
    private FotoPerfilService fotoPerfilService;

    @BeforeEach
    void setUp() {

        usuarioRepository =
                mock(UsuarioRepository.class);

        storageService =
                mock(FotoPerfilStorageService.class);

        fotoPerfilService =
                new FotoPerfilService(
                        usuarioRepository,
                        storageService
                );
    }

    @Test
    void deveDefinirFotoDePerfil() {

        Usuario usuario = new Usuario();

        MockMultipartFile foto =
                criarFoto();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                storageService.armazenar(foto)
        ).thenReturn(
                "nova-foto.jpg"
        );

        Usuario resultado =
                fotoPerfilService.atualizarFoto(
                        1L,
                        foto
                );

        assertSame(
                usuario,
                resultado
        );

        assertEquals(
                "nova-foto.jpg",
                usuario.getProfileImageUrl()
        );

        verify(storageService)
                .armazenar(foto);

        verify(usuarioRepository)
                .saveAndFlush(usuario);

        verify(
                storageService,
                never()
        ).remover(anyString());
    }

    @Test
    void deveSubstituirFotoDePerfilExistente() {

        Usuario usuario = new Usuario();

        usuario.setProfileImageUrl(
                "foto-antiga.png"
        );

        MockMultipartFile foto =
                criarFoto();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                storageService.armazenar(foto)
        ).thenReturn(
                "foto-nova.jpg"
        );

        fotoPerfilService.atualizarFoto(
                1L,
                foto
        );

        assertEquals(
                "foto-nova.jpg",
                usuario.getProfileImageUrl()
        );

        verify(usuarioRepository)
                .saveAndFlush(usuario);

        verify(storageService)
                .remover(
                        "foto-antiga.png"
                );
    }

    @Test
    void deveRemoverNovaFotoQuandoPersistenciaFalhar() {

        Usuario usuario = new Usuario();

        MockMultipartFile foto =
                criarFoto();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                storageService.armazenar(foto)
        ).thenReturn(
                "nova-foto.jpg"
        );

        doThrow(
                new RuntimeException(
                        "Falha simulada no banco"
                )
        ).when(
                usuarioRepository
        ).saveAndFlush(usuario);

        assertThrows(
                RuntimeException.class,
                () ->
                        fotoPerfilService.atualizarFoto(
                                1L,
                                foto
                        )
        );

        verify(storageService)
                .remover(
                        "nova-foto.jpg"
                );
    }

    @Test
    void deveBuscarFotoDePerfil() {

        Usuario usuario = new Usuario();

        usuario.setProfileImageUrl(
                "foto.jpg"
        );

        FotoPerfilArquivo arquivo =
                new FotoPerfilArquivo(
                        new byte[]{
                                1,
                                2,
                                3
                        },
                        MediaType.IMAGE_JPEG
                );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                storageService.carregar(
                        "foto.jpg"
                )
        ).thenReturn(
                arquivo
        );

        FotoPerfilArquivo resultado =
                fotoPerfilService.buscarFoto(
                        1L
                );

        assertSame(
                arquivo,
                resultado
        );

        verify(storageService)
                .carregar(
                        "foto.jpg"
                );
    }

    @Test
    void deveRetornarErroAoBuscarFotoQuandoUsuarioNaoPossuirFoto() {

        Usuario usuario = new Usuario();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        assertThrows(
                FotoPerfilNaoEncontradaException.class,
                () ->
                        fotoPerfilService.buscarFoto(
                                1L
                        )
        );

        verify(
                storageService,
                never()
        ).carregar(anyString());
    }

    @Test
    void deveRemoverFotoDePerfil() {

        Usuario usuario = new Usuario();

        usuario.setProfileImageUrl(
                "foto.jpg"
        );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        Usuario resultado =
                fotoPerfilService.removerFoto(
                        1L
                );

        assertSame(
                usuario,
                resultado
        );

        assertNull(
                usuario.getProfileImageUrl()
        );

        verify(usuarioRepository)
                .saveAndFlush(usuario);

        verify(storageService)
                .remover(
                        "foto.jpg"
                );
    }

    @Test
    void devePermitirRemoverFotoQuandoUsuarioJaNaoPossuirFoto() {

        Usuario usuario = new Usuario();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        Usuario resultado =
                fotoPerfilService.removerFoto(
                        1L
                );

        assertSame(
                usuario,
                resultado
        );

        verify(
                usuarioRepository,
                never()
        ).saveAndFlush(any());

        verify(
                storageService,
                never()
        ).remover(anyString());
    }

    @Test
    void deveManterOperacaoQuandoFalharRemocaoDaFotoAntiga() {

        Usuario usuario = new Usuario();

        usuario.setProfileImageUrl(
                "foto-antiga.jpg"
        );

        MockMultipartFile foto =
                criarFoto();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                storageService.armazenar(foto)
        ).thenReturn(
                "foto-nova.jpg"
        );

        doThrow(
                new FotoPerfilArmazenamentoException(
                        "Falha simulada",
                        new RuntimeException()
                )
        ).when(
                storageService
        ).remover(
                "foto-antiga.jpg"
        );

        Usuario resultado =
                fotoPerfilService.atualizarFoto(
                        1L,
                        foto
                );

        assertEquals(
                "foto-nova.jpg",
                resultado.getProfileImageUrl()
        );

        verify(usuarioRepository)
                .saveAndFlush(usuario);
    }

    @Test
    void deveRetornarErroQuandoUsuarioNaoExistir() {

        MockMultipartFile foto =
                criarFoto();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () ->
                        fotoPerfilService.atualizarFoto(
                                1L,
                                foto
                        )
        );

        verifyNoInteractions(
                storageService
        );
    }

    private MockMultipartFile criarFoto() {

        return new MockMultipartFile(
                "foto",
                "perfil.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{
                        (byte) 0xFF,
                        (byte) 0xD8,
                        (byte) 0xFF
                }
        );
    }

    @Test
    void devePropagarErroQuandoFalharRemocaoDaFotoAtual() {

        Usuario usuario = new Usuario();

        usuario.setProfileImageUrl(
                "foto.jpg"
        );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        FotoPerfilArmazenamentoException exception =
                new FotoPerfilArmazenamentoException(
                        "Falha simulada",
                        new RuntimeException()
                );

        doThrow(exception)
                .when(storageService)
                .remover("foto.jpg");

        FotoPerfilArmazenamentoException resultado =
                assertThrows(
                        FotoPerfilArmazenamentoException.class,
                        () ->
                                fotoPerfilService.removerFoto(
                                        1L
                                )
                );

        assertSame(
                exception,
                resultado
        );

        verify(usuarioRepository)
                .saveAndFlush(usuario);

        verify(storageService)
                .remover("foto.jpg");
    }
}