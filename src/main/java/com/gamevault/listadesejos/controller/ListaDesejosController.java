package com.gamevault.listadesejos.controller;

import com.gamevault.listadesejos.dto.AdicionarItemListaDesejosRequisicao;
import com.gamevault.listadesejos.dto.ItemListaDesejosResposta;
import com.gamevault.listadesejos.entity.ItemListaDesejos;
import com.gamevault.listadesejos.service.ListaDesejosService;
import com.gamevault.user.security.UsuarioPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/me/lista-desejos")
public class ListaDesejosController {

    private final ListaDesejosService listaDesejosService;

    public ListaDesejosController(
            ListaDesejosService listaDesejosService
    ) {
        this.listaDesejosService = listaDesejosService;
    }

    @PostMapping
    public ResponseEntity<ItemListaDesejosResposta> adicionar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody AdicionarItemListaDesejosRequisicao requisicao
    ) {

        ItemListaDesejos item =
                listaDesejosService.adicionar(
                        usuarioPrincipal.getId(),
                        requisicao.rawgGameId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ItemListaDesejosResposta.de(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemListaDesejosResposta>> listar(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
    ) {

        List<ItemListaDesejosResposta> itens =
                listaDesejosService
                        .listar(
                                usuarioPrincipal.getId()
                        )
                        .stream()
                        .map(ItemListaDesejosResposta::de)
                        .toList();

        return ResponseEntity.ok(itens);
    }

    @DeleteMapping("/{rawgGameId}")
    public ResponseEntity<Void> remover(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @PathVariable Long rawgGameId
    ) {
        listaDesejosService.remover(
                usuarioPrincipal.getId(),
                rawgGameId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
