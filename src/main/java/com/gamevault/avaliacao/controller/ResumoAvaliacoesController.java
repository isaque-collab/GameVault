package com.gamevault.avaliacao.controller;

import com.gamevault.avaliacao.dto.ResumoAvaliacoesResposta;
import com.gamevault.avaliacao.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jogos/{rawgGameId}/avaliacoes")
public class ResumoAvaliacoesController {

    private final AvaliacaoService avaliacaoService;

    public ResumoAvaliacoesController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoAvaliacoesResposta> obterResumo(
            @PathVariable Long rawgGameId
    ) {
        Double media = avaliacaoService
                .calcularMediaPorJogo(rawgGameId)
                .orElse(null);

        long quantidade =
                avaliacaoService.contarAvaliacoes(rawgGameId);

        ResumoAvaliacoesResposta resposta =
                new ResumoAvaliacoesResposta(
                        rawgGameId,
                        media,
                        quantidade
                );

        return ResponseEntity.ok(resposta);
    }
}
