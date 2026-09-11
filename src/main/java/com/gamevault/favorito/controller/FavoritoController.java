package com.gamevault.favorito.controller;


import com.gamevault.favorito.dto.AdicionarFavoritoRequisicao;
import com.gamevault.favorito.dto.FavoritoResposta;
import com.gamevault.favorito.entity.Favorito;
import com.gamevault.favorito.service.FavoritoService;
import com.gamevault.user.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/me/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    public ResponseEntity<FavoritoResposta> adicionarFavorito(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody AdicionarFavoritoRequisicao requisicao
    ) {
        Favorito favorito =
                favoritoService.adicionarFavorito(
                        usuarioPrincipal.getId(),
                        requisicao.rawgGameId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        FavoritoResposta.de(favorito)
                );
    }

    @GetMapping
    public ResponseEntity<List<FavoritoResposta>> listarFavoritos(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
    ) {

        List<FavoritoResposta> favoritos =
                favoritoService
                        .listarFavoritos(
                                usuarioPrincipal.getId()
                        )
                        .stream()
                        .map(FavoritoResposta::de)
                        .toList();

        return ResponseEntity.ok(favoritos);
    }

    @DeleteMapping("/{rawgGameId}")
    public ResponseEntity<Void> removerFavorito(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable Long rawgGameId
    ) {

        favoritoService.removerFavorito(
                usuarioPrincipal.getId(),
                rawgGameId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
