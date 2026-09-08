package com.gamevault.favorito.dto;

import com.gamevault.favorito.entity.Favorito;

import java.time.LocalDateTime;

public record FavoritoResposta(
        Long id,
        Long rawgGameId,
        LocalDateTime criadoEm
) {

    public static FavoritoResposta de(Favorito favorito) {
        return new FavoritoResposta(
                favorito.getId(),
                favorito.getRawgGameId(),
                favorito.getCreatedAt()
        );
    }
}
