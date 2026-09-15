package com.gamevault.jogo.dto;

public enum OrdenacaoJogo {

    POPULARIDADE("-added"),
    AVALIACAO_RAWG("-rating"),
    METACRITIC("-metacritic"),
    LANCAMENTO("-released"),
    NOME("name");

    private final String parametroRawg;

    OrdenacaoJogo(String parametroRawg) {
        this.parametroRawg = parametroRawg;
    }

    public String getParametroRawg() {
        return parametroRawg;
    }
}
