package com.gamevault.avaliacao.exception;

public class AvaliacaoNaoEncontradaException extends RuntimeException {
    public AvaliacaoNaoEncontradaException(
            Long usuarioId,
            Long rawgGameId) {
        super(
                "A avaliação do jogo " + rawgGameId +
                        " não foi encontrada para o usuario " + usuarioId
        );
    }
}
