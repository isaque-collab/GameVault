package com.gamevault.avaliacao.controller;

import com.gamevault.avaliacao.dto.AvaliacaoRequisicao;
import com.gamevault.avaliacao.dto.AvaliacaoResposta;
import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PutMapping("/{rawgGameId}")
    public ResponseEntity<AvaliacaoResposta> avaliar(
            @PathVariable Long usuarioId,
            @PathVariable Long rawgGameId,
            @Valid @RequestBody AvaliacaoRequisicao requisicao
    ) {
        Avaliacao avaliacao = avaliacaoService.avaliar(
                usuarioId,
                rawgGameId,
                requisicao.nota()
        );

        return ResponseEntity.ok(
                AvaliacaoResposta.de(avaliacao)
        );
    }

    @GetMapping("/{rawgGameId}")
    public ResponseEntity<AvaliacaoResposta> buscarAvaliacao(
            @PathVariable Long usuarioId,
            @PathVariable Long rawgGameId
    ) {
        Avaliacao avaliacao = avaliacaoService
                .buscarAvaliacaoDoUsuario(
                        usuarioId,
                        rawgGameId
                )
                .orElseThrow(
                        () -> new AvaliacaoNaoEncontradaException(
                                usuarioId,
                                rawgGameId
                        )
                );

        return ResponseEntity.ok(
                AvaliacaoResposta.de(avaliacao));
    }

    @DeleteMapping("/{rawgGameId}")
    public ResponseEntity<Void> removerAvaliacao(
            @PathVariable Long usuarioId,
            @PathVariable Long rawgGameId
    ) {
        avaliacaoService.remover(usuarioId, rawgGameId);

        return ResponseEntity.noContent().build();
    }
}
