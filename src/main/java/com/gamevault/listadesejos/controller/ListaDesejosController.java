package com.gamevault.listadesejos.controller;

import com.gamevault.jogo.dto.ItemColecaoJogoResposta;
import com.gamevault.jogo.service.JogoColecaoService;
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
    private final JogoColecaoService jogoColecaoService;

    public ListaDesejosController(
            ListaDesejosService listaDesejosService,
            JogoColecaoService jogoColecaoService
    ) {
        this.listaDesejosService =
                listaDesejosService;

        this.jogoColecaoService =
                jogoColecaoService;
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
    public ResponseEntity<List<ItemColecaoJogoResposta>>
    listar(
            @AuthenticationPrincipal
            UsuarioPrincipal usuarioPrincipal
    ) {

        List<ItemColecaoJogoResposta> itens =
                listaDesejosService
                        .listar(
                                usuarioPrincipal.getId()
                        )
                        .stream()
                        .map(
                                item ->
                                        jogoColecaoService
                                                .enriquecer(
                                                        item.getId(),
                                                        item.getRawgGameId(),
                                                        item.getCreatedAt()
                                                )
                        )
                        .toList();

        return ResponseEntity.ok(
                itens
        );
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
