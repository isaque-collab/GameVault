package com.gamevault.rawg.exception;

public class JogoRawgNaoEncontradoException extends RuntimeException {
    public JogoRawgNaoEncontradoException(Long rawgGameId) {
        super("Jogo não encontrado na RAWG: " + rawgGameId);
    }
}
