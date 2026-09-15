package com.gamevault.jogo.dto;

import java.time.LocalDate;

public record FiltroCatalogoJogos(

        String nome,

        String genero,

        Integer plataforma,

        String desenvolvedora,

        String publicadora,

        LocalDate lancamentoInicio,

        LocalDate lancamentoFim,

        OrdenacaoJogo ordenacao,

        int pagina

) {
}
