package com.gamevault.rawg.exception;

public class RawgApiKeyNaoConfiguradaException extends RuntimeException {
    public RawgApiKeyNaoConfiguradaException() {
        super("A chave da API RAWG não está configurada.");
    }
}
