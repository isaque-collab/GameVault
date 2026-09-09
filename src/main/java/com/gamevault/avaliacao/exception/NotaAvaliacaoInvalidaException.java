package com.gamevault.avaliacao.exception;

public class NotaAvaliacaoInvalidaException extends RuntimeException {
    public NotaAvaliacaoInvalidaException() {
        super("A nota da avaliação deve ser um valor inteiro entre 1 e 5");
    }
}
