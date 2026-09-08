package com.gamevault.favorito.service;

import com.gamevault.favorito.entity.Favorito;
import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.favorito.repository.FavoritoRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;

    public FavoritoService(
            FavoritoRepository favoritoRepository,
            UsuarioRepository usuarioRepository) {

        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Favorito addFavorite(Long userId, Long rawgGameId) {

        if (favoritoRepository.existsByUserIdAndRawgGameId(userId, rawgGameId)){
            throw new FavoritoJaExisteException(userId, rawgGameId);
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(userId));

        Favorito favorito = new Favorito();
        favorito.setUser(usuario);
        favorito.setRawgGameId(rawgGameId);

        return favoritoRepository.save(favorito);
    }

    @Transactional
    public void removeFavorite(Long userId, Long rawgGameId) {

        Favorito favorito = favoritoRepository
                .findByUserIdAndRawgGameId(userId, rawgGameId)
                .orElseThrow(
                        () -> new FavoritoNaoEncontradoException(userId, rawgGameId)
                );

        favoritoRepository.delete(favorito);
    }

    public List<Favorito> listFavorites(Long userId) {
        return favoritoRepository.findAllByUserId(userId);
    }
}
