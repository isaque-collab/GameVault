package com.gamevault.jogo.controller;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.jogo.dto.JogoDetalhesResposta;
import com.gamevault.jogo.dto.OrdenacaoJogo;
import com.gamevault.jogo.service.JogoService;
import com.gamevault.user.security.UsuarioPrincipal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    private final JogoService jogoService;

    public JogoController(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @GetMapping
    public ResponseEntity<BuscaJogosResposta> buscarJogos(
            @RequestParam(
                    name = "nome",
                    required = false
            )
            @Pattern(
                    regexp = ".*\\S.*",
                    message = "O nome do jogo não pode estar em branco"
            )
            String nome,

            @RequestParam(
                    name = "pagina",
                    defaultValue = "1"
            )
            @Min(
                    value = 1,
                    message = "A página deve ser maior ou igual a 1"
            )
            int pagina,

            @RequestParam(
                    name = "genero",
                    required = false
            )
            String genero,

            @RequestParam(
                    name = "plataforma",
                    required = false
            )
            @Min(
                    value = 1,
                    message = "A plataforma deve possuir um identificador válido"
            )
            Integer plataforma,

            @RequestParam(
                    name = "desenvolvedora",
                    required = false
            )
            String desenvolvedora,

            @RequestParam(
                    name = "publicadora",
                    required = false
            )
            String publicadora,

            @RequestParam(
                    name = "lancamentoInicio",
                    required = false
            )
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate lancamentoInicio,

            @RequestParam(
                    name = "lancamentoFim",
                    required = false
            )
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate lancamentoFim,

            @RequestParam(
                    name = "ordenacao",
                    required = false
            )
            OrdenacaoJogo ordenacao
    ) {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        nome,
                        genero,
                        plataforma,
                        desenvolvedora,
                        publicadora,
                        lancamentoInicio,
                        lancamentoFim,
                        ordenacao,
                        pagina
                );

        BuscaJogosResposta resposta =
                jogoService.buscarJogos(filtro);

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/populares")
    public ResponseEntity<BuscaJogosResposta> buscarJogosPopulares(
            @RequestParam(
                    name = "pagina",
                    defaultValue = "1"
            )
            @Min(
                    value = 1,
                    message = "A página deve ser maior ou igual a 1"
            )
            int pagina
    ) {
        BuscaJogosResposta resposta =
                jogoService.buscarJogosPopulares(pagina);

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/lancamentos-recentes")
    public ResponseEntity<BuscaJogosResposta> buscarLancamentosRecentes(
            @RequestParam(
                    name = "pagina",
                    defaultValue = "1"
            )
            @Min(
                    value = 1,
                    message = "A página deve ser maior ou igual a 1"
            )
            int pagina
    ) {
        BuscaJogosResposta resposta =
                jogoService.buscarLancamentosRecentes(pagina);

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/mais-bem-avaliados")
    public ResponseEntity<BuscaJogosResposta> buscarJogosMaisBemAvaliados(
            @RequestParam(
                    name = "pagina",
                    defaultValue = "1"
            )
            @Min(
                    value = 1,
                    message = "A página deve ser maior ou igual a 1"
            )
            int pagina
    ) {
        BuscaJogosResposta resposta =
                jogoService.buscarJogosMaisBemAvaliados(pagina);

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{rawgGameId}")
    public ResponseEntity<JogoDetalhesResposta> buscarDetalhesJogo(
            @PathVariable
            @Min(
                    value = 1,
                    message = "O identificador do jogo deve ser maior ou igual a 1"
            )
            Long rawgGameId,

            @AuthenticationPrincipal
            UsuarioPrincipal usuarioPrincipal
    ) {
        Long usuarioId =
                usuarioPrincipal == null
                        ? null
                        : usuarioPrincipal.getId();

        JogoDetalhesResposta resposta =
                jogoService.buscarDetalhesJogo(
                        rawgGameId,
                        usuarioId
                );

        return ResponseEntity.ok(resposta);
    }
}
