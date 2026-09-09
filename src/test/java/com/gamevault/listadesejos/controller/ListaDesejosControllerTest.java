package com.gamevault.listadesejos.controller;

import com.gamevault.listadesejos.entity.ItemListaDesejos;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.listadesejos.service.ListaDesejosService;
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
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListaDesejosController.class)
@Import(TratadorGlobalExcecoes.class)
class ListaDesejosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListaDesejosService listaDesejosService;

    @Test
    @WithMockUser
    void deveAdicionarJogoNaListaDeDesejos() throws Exception {

        ItemListaDesejos item = mock(ItemListaDesejos.class);

        when(item.getId()).thenReturn(1L);
        when(item.getRawgGameId()).thenReturn(3498L);
        when(item.getCreatedAt())
                .thenReturn(
                        LocalDateTime.of(
                                2026,
                                9,
                                8,
                                21,
                                0
                        )
                );

        when(listaDesejosService.adicionar(1L, 3498L))
                .thenReturn(item);

        mockMvc.perform(
                        post("/api/usuarios/1/lista-desejos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.rawgGameId").value(3498)
                );
    }

    @Test
    @WithMockUser
    void deveListarJogosDaListaDeDesejosDoUsuario()
            throws Exception {

        ItemListaDesejos item = mock(ItemListaDesejos.class);

        when(item.getId()).thenReturn(1L);
        when(item.getRawgGameId()).thenReturn(3498L);

        when(listaDesejosService.listar(1L))
                .thenReturn(List.of(item));

        mockMvc.perform(
                        get("/api/usuarios/1/lista-desejos")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].rawgGameId").value(3498)
                );
    }

    @Test
    @WithMockUser
    void deveRemoverJogoDaListaDeDesejos()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/usuarios/1/lista-desejos/3498"
                        )
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(listaDesejosService)
                .remover(1L, 3498L);
    }

    @Test
    @WithMockUser
    void deveRetornarConflitoQuandoJogoJaEstaNaListaDeDesejos()
            throws Exception {

        when(listaDesejosService.adicionar(1L, 3498L))
                .thenThrow(
                        new ItemListaDesejosJaExisteException(
                                1L,
                                3498L
                        )
                );

        mockMvc.perform(
                        post("/api/usuarios/1/lista-desejos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Item já existente na lista de desejos"
                                )
                );
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoAoRemoverJogoQueNaoEstaNaListaDeDesejos()
            throws Exception {

        doThrow(
                new ItemListaDesejosNaoEncontradoException(
                        1L,
                        3498L
                )
        ).when(listaDesejosService)
                .remover(1L, 3498L);

        mockMvc.perform(
                        delete(
                                "/api/usuarios/1/lista-desejos/3498"
                        )
                                .with(csrf())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRejeitarRequisicaoSemIdDoJogo()
            throws Exception {

        mockMvc.perform(
                        post("/api/usuarios/1/lista-desejos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(listaDesejosService);
    }
}