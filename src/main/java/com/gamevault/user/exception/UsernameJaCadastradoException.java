package com.gamevault.user.exception;

public class UsernameJaCadastradoException extends RuntimeException {

    public UsernameJaCadastradoException(String username) {
        super("O username já está em uso: " + username);
    }
}
