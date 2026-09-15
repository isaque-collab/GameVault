package com.gamevault.jogo.controller;

import com.gamevault.jogo.dto.BuscaJogosResposta;
import com.gamevault.jogo.dto.JogoResumoResposta;
import com.gamevault.jogo.service.JogoService;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import com.gamevault.shared.config.SecurityConfig;
import com.gamevault.shared.exception.TratadorGlobalExcecoes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JogoController.class)
@Import({
        SecurityConfig.class,
        TratadorGlobalExcecoes.class
})
class JogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JogoService jogoService;

    @Test
    void devePermitirBuscaPublicaDeJogos() throws Exception {
        JogoResumoResposta jogo = new JogoResumoResposta(
                4200L,
                "Portal 2",
                LocalDate.of(2011, 4, 18),
                "https://exemplo.com/portal-2.jpg",
                4.61,
                6900,
                95
        );

        BuscaJogosResposta resposta =
                new BuscaJogosResposta(
                        2,
                        41,
                        List.of(jogo)
                );

        when(jogoService.buscarJogosPorNome("Portal", 2, null))
                .thenReturn(resposta);

        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "Portal")
                                .param("pagina", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(2))
                .andExpect(
                        jsonPath("$.totalResultados").value(41)
                )
                .andExpect(
                        jsonPath("$.jogos[0].rawgGameId")
                                .value(4200)
                )
                .andExpect(
                        jsonPath("$.jogos[0].nome")
                                .value("Portal 2")
                )
                .andExpect(
                        jsonPath("$.jogos[0].dataLancamento")
                                .value("2011-04-18")
                )
                .andExpect(
                        jsonPath("$.jogos[0].imagemFundo")
                                .value(
                                        "https://exemplo.com/portal-2.jpg"
                                )
                )
                .andExpect(
                        jsonPath("$.jogos[0].notaRawg")
                                .value(4.61)
                )
                .andExpect(
                        jsonPath(
                                "$.jogos[0].quantidadeAvaliacoesRawg"
                        ).value(6900)
                )
                .andExpect(
                        jsonPath("$.jogos[0].metacritic")
                                .value(95)
                );

        verify(jogoService)
                .buscarJogosPorNome("Portal", 2, null);
    }

    @Test
    void deveUtilizarPrimeiraPaginaComoPadrao() throws Exception {
        BuscaJogosResposta resposta =
                new BuscaJogosResposta(
                        1,
                        0,
                        List.of()
                );

        when(jogoService.buscarJogosPorNome("Portal", 1, null))
                .thenReturn(resposta);

        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "Portal")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.jogos").isEmpty());

        verify(jogoService)
                .buscarJogosPorNome("Portal", 1, null);
    }

    @Test
    void deveRejeitarBuscaComNomeEmBranco() throws Exception {
        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "   ")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jogoService);
    }

    @Test
    void deveRejeitarBuscaComPaginaInvalida() throws Exception {
        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "Portal")
                                .param("pagina", "0")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jogoService);
    }

    @Test
    void deveRejeitarBuscaSemNome() throws Exception {
        mockMvc.perform(
                        get("/api/jogos")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jogoService);
    }

    @Test
    void deveRetornarBadGatewayQuandoRawgFalhar()
            throws Exception {

        when(jogoService.buscarJogosPorNome("Portal", 1, null))
                .thenThrow(new RawgIntegracaoException(
                        "Não foi possível consultar a RAWG."
                ));

        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "Portal")
                )
                .andExpect(status().isBadGateway())
                .andExpect(
                        jsonPath("$.status").value(502)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Falha na integração com a RAWG")
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Não foi possível consultar a RAWG."
                                )
                );

        verify(jogoService)
                .buscarJogosPorNome("Portal", 1, null);
    }

    @Test
    void deveRetornarServicoIndisponivelQuandoApiKeyNaoEstiverConfigurada()
            throws Exception {

        when(jogoService.buscarJogosPorNome("Portal", 1, null))
                .thenThrow(
                        new RawgApiKeyNaoConfiguradaException()
                );

        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "Portal")
                )
                .andExpect(status().isServiceUnavailable())
                .andExpect(
                        jsonPath("$.status").value(503)
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Integração RAWG indisponível")
                )
                .andExpect(
                        jsonPath("$.detail").value(
                                "A chave da API RAWG não está configurada."
                        )
                );

        verify(jogoService)
                .buscarJogosPorNome("Portal", 1, null);
    }

    @Test
    void deveBuscarJogosFiltrandoPorGenero() throws Exception {
        BuscaJogosResposta resposta =
                new BuscaJogosResposta(
                        1,
                        0,
                        List.of()
                );

        when(jogoService.buscarJogosPorNome(
                "Portal",
                1,
                "action"
        )).thenReturn(resposta);

        mockMvc.perform(
                        get("/api/jogos")
                                .param("nome", "Portal")
                                .param("genero", "action")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.jogos").isEmpty());

        verify(jogoService).buscarJogosPorNome(
                "Portal",
                1,
                "action"
        );
    }
}
