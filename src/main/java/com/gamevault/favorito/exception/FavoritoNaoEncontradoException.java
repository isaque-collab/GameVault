package com.gamevault.favorito.exception;

public class FavoritoNaoEncontradoException extends RuntimeException {
    public FavoritoNaoEncontradoException(Long userId, Long rawgGameId) {
        super(
                "O jogo " + rawgGameId +
                        " não está nos favoritos do usuário " + userId
        );
    }
}
