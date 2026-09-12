package com.gamevault.avaliacao.controller;

import com.gamevault.avaliacao.dto.AvaliacaoRequisicao;
import com.gamevault.avaliacao.dto.AvaliacaoResposta;
import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.service.AvaliacaoService;
import com.gamevault.user.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/me/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PutMapping("/{rawgGameId}")
    public ResponseEntity<AvaliacaoResposta> avaliar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable Long rawgGameId,
            @Valid @RequestBody AvaliacaoRequisicao requisicao
    ) {
        Avaliacao avaliacao = avaliacaoService.avaliar(
                usuarioPrincipal.getId(),
                rawgGameId,
                requisicao.nota()
        );

        return ResponseEntity.ok(
                AvaliacaoResposta.de(avaliacao)
        );
    }

    @GetMapping("/{rawgGameId}")
    public ResponseEntity<AvaliacaoResposta> buscarAvaliacao(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable Long rawgGameId
    ) {
        Avaliacao avaliacao = avaliacaoService
                .buscarAvaliacaoDoUsuario(
                        usuarioPrincipal.getId(),
                        rawgGameId
                )
                .orElseThrow(
                        () -> new AvaliacaoNaoEncontradaException(
                                usuarioPrincipal.getId(),
                                rawgGameId
                        )
                );

        return ResponseEntity.ok(
                AvaliacaoResposta.de(avaliacao));
    }

    @DeleteMapping("/{rawgGameId}")
    public ResponseEntity<Void> removerAvaliacao(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable Long rawgGameId
    ) {
        avaliacaoService.remover(usuarioPrincipal.getId(), rawgGameId);

        return ResponseEntity.noContent().build();
    }
}
