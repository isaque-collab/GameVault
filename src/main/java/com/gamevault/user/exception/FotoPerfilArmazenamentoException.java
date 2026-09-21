package com.gamevault.user.exception;

public class FotoPerfilArmazenamentoException extends RuntimeException {
    public FotoPerfilArmazenamentoException(
            String mensagem,
            Throwable causa
    ) {
        super(mensagem, causa);
    }
}
