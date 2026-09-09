package com.gamevault.listadesejos.exception;

public class ItemListaDesejosNaoEncontradoException extends RuntimeException {
    public ItemListaDesejosNaoEncontradoException(
            Long userId,
            Long rawgGameId
    ) {
        super(
                "O jogo " + rawgGameId +
                   " não foi encontrado na lista de desejos do usuário " + userId
        );
    }
}
