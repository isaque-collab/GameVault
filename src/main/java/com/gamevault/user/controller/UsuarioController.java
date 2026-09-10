package com.gamevault.user.controller;

import com.gamevault.user.dto.CadastroUsuarioRequisicao;
import com.gamevault.user.dto.UsuarioResposta;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResposta> cadastrar(
            @Valid @RequestBody CadastroUsuarioRequisicao requisicao
    ) {
        Usuario usuario = usuarioService.cadastrar(
                requisicao.nome(),
                requisicao.username(),
                requisicao.email(),
                requisicao.senha(),
                requisicao.confirmacaoSenha()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UsuarioResposta.de(usuario));
    }
}
