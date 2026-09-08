package com.gamevault.favorito.exception;

public class FavoritoJaExisteException extends RuntimeException {

    public FavoritoJaExisteException(Long userId, Long rawgGameId) {
        super(
                "O jogo " + rawgGameId +
                        " já está nos favoritos do usuário " + userId
        );
    }
}
