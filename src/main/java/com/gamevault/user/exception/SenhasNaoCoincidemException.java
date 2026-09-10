package com.gamevault.user.exception;

public class SenhasNaoCoincidemException extends RuntimeException {
    public SenhasNaoCoincidemException() {
        super("A senha e a confirmação de senha não coincidem");
    }
}
