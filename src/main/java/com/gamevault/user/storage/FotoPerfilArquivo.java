package com.gamevault.user.storage;

import org.springframework.http.MediaType;

public record FotoPerfilArquivo(
        byte[] conteudo,
        MediaType tipoConteudo
) {
}
