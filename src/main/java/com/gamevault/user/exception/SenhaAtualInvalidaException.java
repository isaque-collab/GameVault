package com.gamevault.user.exception;

public class SenhaAtualInvalidaException extends RuntimeException {
    public SenhaAtualInvalidaException() {
        super("A senha atual está incorreta");
    }
}
