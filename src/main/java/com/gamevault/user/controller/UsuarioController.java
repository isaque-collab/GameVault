package com.gamevault.user.controller;

import com.gamevault.user.dto.AlteracaoSenhaRequisicao;
import com.gamevault.user.dto.AtualizacaoPerfilRequisicao;
import com.gamevault.user.dto.CadastroUsuarioRequisicao;
import com.gamevault.user.dto.UsuarioResposta;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.security.UsuarioPrincipal;
import com.gamevault.user.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/me")
    public ResponseEntity<UsuarioResposta> buscarPerfil(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal
    ) {
        Usuario usuario =
                usuarioService.buscarPorId(
                        usuarioPrincipal.getId()
                );

        return ResponseEntity.ok(
                UsuarioResposta.de(usuario)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResposta> atualizarPerfil(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody AtualizacaoPerfilRequisicao requisicao
    ) {
        Usuario usuario =
                usuarioService.atualizarPerfil(
                        usuarioPrincipal.getId(),
                        requisicao.nome(),
                        requisicao.username(),
                        requisicao.email(),
                        requisicao.imagemPerfil()
                );

        return ResponseEntity.ok(
                UsuarioResposta.de(usuario)
        );
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            @Valid @RequestBody AlteracaoSenhaRequisicao requisicao
    ) {
        usuarioService.alterarSenha(
                usuarioPrincipal.getId(),
                requisicao.senhaAtual(),
                requisicao.novaSenha(),
                requisicao.confirmacaoNovaSenha()
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> excluirConta(
            @AuthenticationPrincipal UsuarioPrincipal usuarioPrincipal,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        usuarioService.excluirConta(
                usuarioPrincipal.getId()
        );

        SecurityContextLogoutHandler logoutHandler =
                new SecurityContextLogoutHandler();

        logoutHandler.logout(
                request,
                response,
                authentication
        );

        return ResponseEntity.noContent().build();
    }
}
