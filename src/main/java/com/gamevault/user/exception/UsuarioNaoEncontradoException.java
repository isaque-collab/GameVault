package com.gamevault.user.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(Long userId) {
        super("Usuário não encontrado: " + userId);
    }
}
