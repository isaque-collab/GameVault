package com.gamevault.avaliacao.controller;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.service.AvaliacaoService;
import com.gamevault.shared.exception.TratadorGlobalExcecoes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvaliacaoController.class)
@Import(TratadorGlobalExcecoes.class)
class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    @Test
    @WithMockUser
    void deveAvaliarJogo() throws Exception {
        Avaliacao avaliacao = mock(Avaliacao.class);

        when(avaliacao.getId()).thenReturn(1L);
        when(avaliacao.getRawgGameId()).thenReturn(3498L);
        when(avaliacao.getRating()).thenReturn((byte) 5);
        when(avaliacao.getCreatedAt())
                .thenReturn(LocalDateTime.of(2026, 9, 9, 20, 0));
        when(avaliacao.getUpdatedAt())
                .thenReturn(LocalDateTime.of(2026, 9, 9, 20, 0));

        when(
                avaliacaoService.avaliar(
                        1L,
                        3498L,
                        (byte) 5
                )
        ).thenReturn(avaliacao);

        mockMvc.perform(
                        put("/api/usuarios/1/avaliacoes/3498")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nota": 5
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rawgGameId").value(3498))
                .andExpect(jsonPath("$.nota").value(5));

        verify(avaliacaoService)
                .avaliar(1L, 3498L, (byte) 5);
    }

    @Test
    @WithMockUser
    void deveBuscarAvaliacaoDoUsuario() throws Exception {
        Avaliacao avaliacao = mock(Avaliacao.class);

        when(avaliacao.getId()).thenReturn(1L);
        when(avaliacao.getRawgGameId()).thenReturn(3498L);
        when(avaliacao.getRating()).thenReturn((byte) 4);

        when(
                avaliacaoService.buscarAvaliacaoDoUsuario(
                        1L,
                        3498L
                )
        ).thenReturn(Optional.of(avaliacao));

        mockMvc.perform(
                        get("/api/usuarios/1/avaliacoes/3498")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rawgGameId").value(3498))
                .andExpect(jsonPath("$.nota").value(4));
    }

    @Test
    @WithMockUser
    void deveRemoverAvaliacao() throws Exception {
        mockMvc.perform(
                        delete("/api/usuarios/1/avaliacoes/3498")
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(avaliacaoService)
                .remover(1L, 3498L);
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoAoBuscarAvaliacaoInexistente()
            throws Exception {

        when(
                avaliacaoService.buscarAvaliacaoDoUsuario(
                        1L,
                        3498L
                )
        ).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/usuarios/1/avaliacoes/3498")
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.title")
                                .value("Recurso não encontrado")
                );
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoAoRemoverAvaliacaoInexistente()
            throws Exception {

        doThrow(
                new AvaliacaoNaoEncontradaException(
                        1L,
                        3498L
                )
        ).when(avaliacaoService)
                .remover(1L, 3498L);

        mockMvc.perform(
                        delete("/api/usuarios/1/avaliacoes/3498")
                                .with(csrf())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRejeitarNotaAbaixoDoMinimo() throws Exception {
        mockMvc.perform(
                        put("/api/usuarios/1/avaliacoes/3498")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nota": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(avaliacaoService);
    }

    @Test
    @WithMockUser
    void deveRejeitarNotaAcimaDoMaximo() throws Exception {
        mockMvc.perform(
                        put("/api/usuarios/1/avaliacoes/3498")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nota": 6
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(avaliacaoService);
    }

    @Test
    @WithMockUser
    void deveRejeitarRequisicaoSemNota() throws Exception {
        mockMvc.perform(
                        put("/api/usuarios/1/avaliacoes/3498")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(avaliacaoService);
    }
}