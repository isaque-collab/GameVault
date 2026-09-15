package com.gamevault.jogo.controller;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.jogo.dto.OrdenacaoJogo;
import com.gamevault.jogo.service.JogoService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
