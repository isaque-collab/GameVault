package com.gamevault.jogo.controller;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.service.JogoService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    private final JogoService jogoService;

    public JogoController(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @GetMapping
    public ResponseEntity<BuscaJogosResposta> buscarJogos(
            @RequestParam(name = "nome")
            @NotBlank(message = "O nome do jogo é obrigatório")
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
            String genero
    ) {
        BuscaJogosResposta resposta =
                jogoService.buscarJogosPorNome(
                        nome,
                        pagina,
                        genero
                );

        return ResponseEntity.ok(resposta);
    }
}
