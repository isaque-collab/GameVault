package com.gamevault.user.storage;

import com.gamevault.user.exception.FotoPerfilInvalidaException;
import com.gamevault.user.exception.FotoPerfilMuitoGrandeException;
import com.gamevault.user.exception.FotoPerfilNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FotoPerfilStorageServiceTest {

    @TempDir
    Path diretorioTemporario;

    private FotoPerfilStorageService storageService;

    @BeforeEach
    void setUp() {

        storageService =
                new FotoPerfilStorageService(
                        diretorioTemporario.toString()
                );
    }

    @Test
    void deveArmazenarFotoJpeg() throws Exception {

        byte[] conteudo = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x01,
                0x02
        };

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        conteudo
                );

        String chave =
                storageService.armazenar(foto);

        assertTrue(
                chave.endsWith(".jpg")
        );

        assertTrue(
                Files.exists(
                        diretorioTemporario.resolve(chave)
                )
        );

        assertArrayEquals(
                conteudo,
                Files.readAllBytes(
                        diretorioTemporario.resolve(chave)
                )
        );
    }

    @Test
    void deveArmazenarFotoPng() {

        byte[] conteudo = {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
                0x01
        };

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.png",
                        MediaType.IMAGE_PNG_VALUE,
                        conteudo
                );

        String chave =
                storageService.armazenar(foto);

        assertTrue(
                chave.endsWith(".png")
        );
    }

    @Test
    void deveArmazenarFotoWebp() {

        byte[] conteudo = {
                'R', 'I', 'F', 'F',
                0x01, 0x02, 0x03, 0x04,
                'W', 'E', 'B', 'P',
                0x01
        };

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.webp",
                        "image/webp",
                        conteudo
                );

        String chave =
                storageService.armazenar(foto);

        assertTrue(
                chave.endsWith(".webp")
        );
    }

    @Test
    void deveRejeitarFotoVazia() {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        new byte[0]
                );

        assertThrows(
                FotoPerfilInvalidaException.class,
                () -> storageService.armazenar(foto)
        );
    }

    @Test
    void deveRejeitarFotoMaiorQueDoisMegabytes() {

        byte[] conteudo =
                new byte[
                        (2 * 1024 * 1024) + 1
                        ];

        conteudo[0] = (byte) 0xFF;
        conteudo[1] = (byte) 0xD8;
        conteudo[2] = (byte) 0xFF;

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        conteudo
                );

        assertThrows(
                FotoPerfilMuitoGrandeException.class,
                () -> storageService.armazenar(foto)
        );
    }

    @Test
    void deveRejeitarConteudoQueNaoSejaImagemPermitida() {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        "nao-e-uma-imagem".getBytes()
                );

        assertThrows(
                FotoPerfilInvalidaException.class,
                () -> storageService.armazenar(foto)
        );
    }

    @Test
    void deveCarregarFotoArmazenada() {

        byte[] conteudo = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x01
        };

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        conteudo
                );

        String chave =
                storageService.armazenar(foto);

        FotoPerfilArquivo arquivo =
                storageService.carregar(chave);

        assertAll(
                () -> assertArrayEquals(
                        conteudo,
                        arquivo.conteudo()
                ),
                () -> assertEquals(
                        MediaType.IMAGE_JPEG,
                        arquivo.tipoConteudo()
                )
        );
    }

    @Test
    void deveRemoverFotoArmazenada() {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        new byte[]{
                                (byte) 0xFF,
                                (byte) 0xD8,
                                (byte) 0xFF
                        }
                );

        String chave =
                storageService.armazenar(foto);

        Path arquivo =
                diretorioTemporario.resolve(chave);

        assertTrue(
                Files.exists(arquivo)
        );

        storageService.remover(chave);

        assertFalse(
                Files.exists(arquivo)
        );
    }

    @Test
    void deveRetornarErroQuandoFotoNaoExistir() {

        assertThrows(
                FotoPerfilNaoEncontradaException.class,
                () -> storageService.carregar(
                        "foto-inexistente.jpg"
                )
        );
    }
}

