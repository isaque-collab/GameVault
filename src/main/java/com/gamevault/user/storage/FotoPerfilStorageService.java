package com.gamevault.user.storage;

import com.gamevault.user.exception.FotoPerfilArmazenamentoException;
import com.gamevault.user.exception.FotoPerfilInvalidaException;
import com.gamevault.user.exception.FotoPerfilMuitoGrandeException;
import com.gamevault.user.exception.FotoPerfilNaoEncontradaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class FotoPerfilStorageService {

    private static final long TAMANHO_MAXIMO_BYTES =
            2L * 1024 * 1024;

    private final Path diretorioBase;

    public FotoPerfilStorageService(
            @Value("${gamevault.profile-image.directory}")
            String diretorio
    ) {
        this.diretorioBase =
                Path.of(diretorio)
                .toAbsolutePath()
                .normalize();

        criarDiretorio();
    }

    public String armazenar(MultipartFile foto) {

        validarArquivo(foto);

        byte[] conteudo =
                lerConteudo(foto);

        String extensao =
                detectarExtensao(conteudo);

        String chave =
                UUID.randomUUID() + extensao;

        Path destino =
                resolverCaminho(chave);

        try {
            Files.write(
                    destino,
                    conteudo,
                    StandardOpenOption.CREATE_NEW
            );

            return chave;

        } catch (IOException exception) {
            throw new FotoPerfilArmazenamentoException(
                    "Não foi possível armazenar a foto de perfil.",
                    exception
            );
        }
    }

    public FotoPerfilArquivo carregar(String chave) {

        Path arquivo =
                resolverCaminho(chave);

        if (!Files.isRegularFile(arquivo)) {
            throw new FotoPerfilNaoEncontradaException();
        }

        try {
            return new FotoPerfilArquivo(
                    Files.readAllBytes(arquivo),
                    identificarMediaType(chave)
            );

        } catch (IOException exception) {
            throw new FotoPerfilArmazenamentoException(
                    "Não foi possível carregar a foto de perfil.",
                    exception
            );
        }
    }

    public void remover(String chave) {

        if (chave == null || chave.isBlank()) {
            return;
        }

        Path arquivo =
                resolverCaminho(chave);

        try {
            Files.deleteIfExists(arquivo);

        } catch (IOException exception) {
            throw new FotoPerfilArmazenamentoException(
                    "Não foi possível remover a foto de perfil.",
                    exception
            );
        }
    }

    private void validarArquivo(MultipartFile foto) {

        if (foto == null || foto.isEmpty()) {
            throw new FotoPerfilInvalidaException(
                    "A foto de perfil é obrigatória."
            );
        }

        if (foto.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new FotoPerfilMuitoGrandeException();
        }
    }

    private byte[] lerConteudo(MultipartFile foto) {

        try {
            return foto.getBytes();

        } catch (IOException exception) {
            throw new FotoPerfilArmazenamentoException(
                    "Não foi possível ler a foto de perfil.",
                    exception
            );
        }
    }

    private String detectarExtensao(byte[] conteudo) {

        if (ehJpeg(conteudo)) {
            return ".jpg";
        }

        if (ehPng(conteudo)) {
            return ".png";
        }

        if (ehWebp(conteudo)) {
            return ".webp";
        }

        throw new FotoPerfilInvalidaException(
                "A foto deve estar no formato JPEG, PNG ou WebP."
        );
    }

    private boolean ehJpeg(byte[] conteudo) {

        return conteudo.length >= 3
                && (conteudo[0] & 0xFF) == 0xFF
                && (conteudo[1] & 0xFF) == 0xD8
                && (conteudo[2] & 0xFF) == 0xFF;
    }

    private boolean ehPng(byte[] conteudo) {

        byte[] assinatura = {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };

        if (conteudo.length < assinatura.length) {
            return false;
        }

        for (int i = 0; i < assinatura.length; i++) {

            if (conteudo[i] != assinatura[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean ehWebp(byte[] conteudo) {

        return conteudo.length >= 12
                && conteudo[0] == 'R'
                && conteudo[1] == 'I'
                && conteudo[2] == 'F'
                && conteudo[3] == 'F'
                && conteudo[8] == 'W'
                && conteudo[9] == 'E'
                && conteudo[10] == 'B'
                && conteudo[11] == 'P';
    }

    private MediaType identificarMediaType(
            String chave
    ) {

        if (chave.endsWith(".jpg")) {
            return MediaType.IMAGE_JPEG;
        }

        if (chave.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        if (chave.endsWith(".webp")) {
            return MediaType.parseMediaType(
                    "image/webp"
            );
        }

        throw new FotoPerfilInvalidaException(
                "Formato da foto de perfil inválido."
        );
    }

    private Path resolverCaminho(String chave) {

        Path caminho =
                diretorioBase
                        .resolve(chave)
                        .normalize();

        if (!caminho.startsWith(diretorioBase)) {
            throw new FotoPerfilInvalidaException(
                    "Referência da foto de perfil inválida."
            );
        }

        return caminho;
    }

    private void criarDiretorio() {

        try {
            Files.createDirectories(
                    diretorioBase
            );

        } catch (IOException exception) {
            throw new FotoPerfilArmazenamentoException(
                    "Não foi possível preparar o diretório das fotos de perfil.",
                    exception
            );
        }
    }
}
