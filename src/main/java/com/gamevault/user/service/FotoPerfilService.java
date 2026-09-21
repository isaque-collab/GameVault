package com.gamevault.user.service;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.FotoPerfilArmazenamentoException;
import com.gamevault.user.exception.FotoPerfilNaoEncontradaException;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import com.gamevault.user.storage.FotoPerfilArquivo;
import com.gamevault.user.storage.FotoPerfilStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FotoPerfilService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FotoPerfilService.class);

    private final UsuarioRepository usuarioRepository;
    private final FotoPerfilStorageService storageService;

    public FotoPerfilService(
            UsuarioRepository usuarioRepository,
            FotoPerfilStorageService storageService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.storageService = storageService;
    }

    @Transactional
    public Usuario atualizarFoto(
            Long usuarioId,
            MultipartFile foto
    ) {

        Usuario usuario =
                buscarUsuario(usuarioId);

        String chaveAnterior =
                usuario.getProfileImageUrl();

        String novaChave =
                storageService.armazenar(foto);

        try {
            usuario.setProfileImageUrl(
                    novaChave
            );

            usuarioRepository.saveAndFlush(
                    usuario
            );

        } catch (RuntimeException exception) {

            removerArquivoSemInterromper(
                    novaChave
            );

            throw exception;
        }

        if (chaveAnterior != null) {
            removerArquivoSemInterromper(
                    chaveAnterior
            );
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public FotoPerfilArquivo buscarFoto(
            Long usuarioId
    ) {

        Usuario usuario =
                buscarUsuario(usuarioId);

        String chave =
                usuario.getProfileImageUrl();

        if (chave == null || chave.isBlank()) {
            throw new FotoPerfilNaoEncontradaException();
        }

        return storageService.carregar(chave);
    }

    @Transactional
    public Usuario removerFoto(
            Long usuarioId
    ) {

        Usuario usuario =
                buscarUsuario(usuarioId);

        String chave =
                usuario.getProfileImageUrl();

        if (chave == null || chave.isBlank()) {
            return  usuario;
        }

        usuario.setProfileImageUrl(null);

        usuarioRepository.saveAndFlush(
                usuario
        );

        storageService.remover(chave);

        return usuario;
    }

    private Usuario buscarUsuario(
            Long usuarioId
    ) {

        return usuarioRepository
                .findById(usuarioId)
                .orElseThrow(
                        () ->
                                new UsuarioNaoEncontradoException(
                                        usuarioId
                                )
                );
    }

    private void removerArquivoSemInterromper(
            String chave
    ) {

        try {
            storageService.remover(chave);

        } catch (FotoPerfilArmazenamentoException exception) {

            LOGGER.warn(
                    "Não foi possível remover arquivo antigo de foto de perfil: {}",
                    chave,
                    exception
            );
        }
    }


}
