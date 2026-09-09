package com.gamevault.listadesejos.dto;

import com.gamevault.listadesejos.entity.ItemListaDesejos;

import java.time.LocalDateTime;

public record ItemListaDesejosResposta(
        Long id,
        Long rawgGameId,
        LocalDateTime criadoEm
) {

    public static ItemListaDesejosResposta de(
            ItemListaDesejos item
    ){
        return new ItemListaDesejosResposta(
                item.getId(),
                item.getRawgGameId(),
                item.getCreatedAt()
        );
    }
}
