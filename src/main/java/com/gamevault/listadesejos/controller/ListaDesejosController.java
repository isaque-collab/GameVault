package com.gamevault.listadesejos.controller;

import com.gamevault.listadesejos.dto.AdicionarItemListaDesejosRequisicao;
import com.gamevault.listadesejos.dto.ItemListaDesejosResposta;
import com.gamevault.listadesejos.entity.ItemListaDesejos;
import com.gamevault.listadesejos.service.ListaDesejosService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/lista-desejos")
public class ListaDesejosController {

    private final ListaDesejosService listaDesejosService;

    public ListaDesejosController(
            ListaDesejosService listaDesejosService
    ) {
        this.listaDesejosService = listaDesejosService;
    }

    @PostMapping
    public ResponseEntity<ItemListaDesejosResposta> adicionar(
            @PathVariable Long usuarioId,
            @Valid @RequestBody AdicionarItemListaDesejosRequisicao requisicao
    ) {

        ItemListaDesejos item = listaDesejosService.adicionar(
                usuarioId,
                requisicao.rawgGameId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ItemListaDesejosResposta.de(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemListaDesejosResposta>> listar(
            @PathVariable Long usuarioId
    ){

        List<ItemListaDesejosResposta> itens =
                listaDesejosService
                        .listar(usuarioId)
                        .stream()
                        .map(ItemListaDesejosResposta::de)
                        .toList();

        return ResponseEntity.ok(itens);
    }

    @DeleteMapping("/{rawgGameId}")
    public ResponseEntity<Void> remover(
            @PathVariable Long usuarioId,
            @PathVariable Long rawgGameId
    ){
        listaDesejosService.remover(
                usuarioId,
                rawgGameId);

        return ResponseEntity.noContent().build();
    }
}
