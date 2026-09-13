package com.gamevault.rawg.exception;

public class RawgIntegracaoException extends RuntimeException {
    public RawgIntegracaoException(String mensagem) {
        super(mensagem);
    }

    public RawgIntegracaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
