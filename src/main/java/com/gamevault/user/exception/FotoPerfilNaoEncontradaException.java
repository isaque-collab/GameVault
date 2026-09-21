package com.gamevault.user.exception;

public class FotoPerfilNaoEncontradaException extends RuntimeException {
    public FotoPerfilNaoEncontradaException() {
        super("Foto de perfil não encontrada.");
    }
}
