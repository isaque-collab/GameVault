package com.gamevault.listadesejos.exception;

public class ItemListaDesejosJaExisteException extends RuntimeException {
    public ItemListaDesejosJaExisteException(
            Long userId,
            Long rawgGameId
    ) {
        super(
                "O jogo " + rawgGameId +
                        " já está na lista de desejos do usuário " + userId
        );
    }
}
