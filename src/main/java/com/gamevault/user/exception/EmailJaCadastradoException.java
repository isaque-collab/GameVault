package com.gamevault.user.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException(String email) {
        super("O e-mail já está em uso: " + email);
    }
}
