package com.gamevault.favorito.controller;


import com.gamevault.favorito.dto.AdicionarFavoritoRequisicao;
import com.gamevault.favorito.dto.FavoritoResposta;
import com.gamevault.favorito.entity.Favorito;
import com.gamevault.favorito.service.FavoritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    public ResponseEntity<FavoritoResposta> adicionarFavorito(
            @PathVariable Long usuarioId,
            @Valid @RequestBody AdicionarFavoritoRequisicao requisicao
            ) {
        Favorito favorito = favoritoService.adicionarFavorito(
                usuarioId,
                requisicao.rawgGameId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FavoritoResposta.de(favorito));
    }

    @GetMapping
    public ResponseEntity<List<FavoritoResposta>> listarFavoritos(
            @PathVariable Long usuarioId
    ){

        List<FavoritoResposta> favoritos = favoritoService
                .listarFavoritos(usuarioId)
                .stream()
                .map(FavoritoResposta::de)
                .toList();

        return ResponseEntity.ok(favoritos);
    }

    @DeleteMapping("/{rawgGameId}")
    public ResponseEntity<Void> removerFavorito(
            @PathVariable Long usuarioId,
            @PathVariable Long rawgGameId
    ){

        favoritoService.removerFavorito(usuarioId, rawgGameId);

        return ResponseEntity.noContent().build();
    }
}
