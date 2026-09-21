package com.gamevault.user.exception;

public class FotoPerfilMuitoGrandeException extends RuntimeException {
    public FotoPerfilMuitoGrandeException() {
        super(
                "A foto de perfil deve possuir no máximo 2 MB."
        );
    }
}
