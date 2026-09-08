package com.gamevault.favorito.controller;

import com.gamevault.favorito.entity.Favorito;
import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.favorito.service.FavoritoService;
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

@WebMvcTest(FavoritoController.class)
@Import(TratadorGlobalExcecoes.class)
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoritoService favoritoService;

    @Test
    @WithMockUser
    void deveAdicionarFavorito() throws Exception {

        Favorito favorito = mock(Favorito.class);

        when(favorito.getId()).thenReturn(1L);
        when(favorito.getRawgGameId()).thenReturn(3498L);
        when(favorito.getCreatedAt())
                .thenReturn(LocalDateTime.of(2026, 9, 8, 16, 0));

        when(favoritoService.adicionarFavorito(1L, 3498L))
                .thenReturn(favorito);

        mockMvc.perform(
                        post("/api/usuarios/1/favoritos")
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
                .andExpect(jsonPath("$.rawgGameId").value(3498));
    }

    @Test
    @WithMockUser
    void deveListarFavoritosDoUsuario() throws Exception {

        Favorito favorito = mock(Favorito.class);

        when(favorito.getId()).thenReturn(1L);
        when(favorito.getRawgGameId()).thenReturn(3498L);

        when(favoritoService.listFavorites(1L))
                .thenReturn(List.of(favorito));

        mockMvc.perform(
                        get("/api/usuarios/1/favoritos")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rawgGameId").value(3498));
    }

    @Test
    @WithMockUser
    void deveRemoverFavorito() throws Exception {

        mockMvc.perform(
                        delete("/api/usuarios/1/favoritos/3498")
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        verify(favoritoService)
                .removeFavorito(1L, 3498L);
    }

    @Test
    @WithMockUser
    void deveRetornarConflitoQuandoFavoritoJaExiste() throws Exception {

        when(favoritoService.adicionarFavorito(1L, 3498L))
                .thenThrow(
                        new FavoritoJaExisteException(1L, 3498L)
                );

        mockMvc.perform(
                        post("/api/usuarios/1/favoritos")
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
                                .value("Favorito já existente")
                );
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoAoRemoverFavoritoInexistente()
            throws Exception {

        doThrow(
                new FavoritoNaoEncontradoException(1L, 3498L)
        ).when(favoritoService)
                .removeFavorito(1L, 3498L);

        mockMvc.perform(
                        delete("/api/usuarios/1/favoritos/3498")
                                .with(csrf())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRejeitarRequisicaoSemIdDoJogo() throws Exception {

        mockMvc.perform(
                        post("/api/usuarios/1/favoritos")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(favoritoService);
    }
}