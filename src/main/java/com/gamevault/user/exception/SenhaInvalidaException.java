package com.gamevault.user.exception;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException() {
        super("A senha deve possuir no mínimo 8 caracteres");
    }
}
