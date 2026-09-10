package com.gamevault.avaliacao.dto;

import com.gamevault.avaliacao.entity.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResposta(
        Long id,
        Long rawgGameId,
        Byte nota,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static AvaliacaoResposta de(Avaliacao avaliacao){
        return new AvaliacaoResposta(
                avaliacao.getId(),
                avaliacao.getRawgGameId(),
                avaliacao.getRating(),
                avaliacao.getCreatedAt(),
                avaliacao.getUpdatedAt()
        );
    }
}
