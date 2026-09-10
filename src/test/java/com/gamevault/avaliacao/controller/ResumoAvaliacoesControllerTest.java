package com.gamevault.avaliacao.controller;

import com.gamevault.avaliacao.service.AvaliacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumoAvaliacoesController.class)
class ResumoAvaliacoesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    @Test
    @WithMockUser
    void deveRetornarResumoDasAvaliacoesDoJogo() throws Exception {
        Long rawgGameId = 3498L;

        when(avaliacaoService.calcularMediaPorJogo(rawgGameId))
                .thenReturn(Optional.of(4.5));

        when(avaliacaoService.contarAvaliacoes(rawgGameId))
                .thenReturn(12L);

        mockMvc.perform(
                        get("/api/jogos/3498/avaliacoes/resumo")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawgGameId").value(3498))
                .andExpect(jsonPath("$.media").value(4.5))
                .andExpect(jsonPath("$.quantidade").value(12));

        verify(avaliacaoService)
                .calcularMediaPorJogo(rawgGameId);

        verify(avaliacaoService)
                .contarAvaliacoes(rawgGameId);
    }

    @Test
    @WithMockUser
    void deveRetornarResumoSemMediaQuandoJogoNaoPossuirAvaliacoes()
            throws Exception {

        Long rawgGameId = 3498L;

        when(avaliacaoService.calcularMediaPorJogo(rawgGameId))
                .thenReturn(Optional.empty());

        when(avaliacaoService.contarAvaliacoes(rawgGameId))
                .thenReturn(0L);

        mockMvc.perform(
                        get("/api/jogos/3498/avaliacoes/resumo")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawgGameId").value(3498))
                .andExpect(jsonPath("$.media").value(nullValue()))
                .andExpect(jsonPath("$.quantidade").value(0));
    }
}