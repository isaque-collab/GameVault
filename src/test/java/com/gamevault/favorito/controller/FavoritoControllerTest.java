package com.gamevault.favorito.controller;

import com.gamevault.favorito.entity.Favorito;
import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.favorito.service.FavoritoService;
import com.gamevault.jogo.dto.ItemColecaoJogoResposta;
import com.gamevault.jogo.service.JogoColecaoService;
import com.gamevault.shared.exception.TratadorGlobalExcecoes;
import com.gamevault.user.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoritoController.class)
@Import(TratadorGlobalExcecoes.class)
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoritoService favoritoService;

    @MockitoBean
    private JogoColecaoService jogoColecaoService;

    @Test
    void deveAdicionarFavorito() throws Exception {

        Favorito favorito = mock(Favorito.class);

        when(favorito.getId())
                .thenReturn(1L);

        when(favorito.getRawgGameId())
                .thenReturn(3498L);

        when(favorito.getCreatedAt())
                .thenReturn(
                        LocalDateTime.of(
                                2026,
                                9,
                                8,
                                16,
                                0
                        )
                );

        when(
                favoritoService.adicionarFavorito(
                        1L,
                        3498L
                )
        ).thenReturn(favorito);

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.rawgGameId")
                                .value(3498)
                );

        verify(favoritoService)
                .adicionarFavorito(
                        1L,
                        3498L
                );
    }

    @Test
    void deveListarFavoritosDoUsuarioAutenticado()
            throws Exception {
        Favorito favorito =
                mock(Favorito.class);

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(favorito.getId())
                .thenReturn(10L);

        when(favorito.getRawgGameId())
                .thenReturn(3498L);

        when(favorito.getCreatedAt())
                .thenReturn(criadoEm);

        when(
                favoritoService.listarFavoritos(1L)
        ).thenReturn(
                List.of(favorito)
        );

        ItemColecaoJogoResposta itemEnriquecido =
                new ItemColecaoJogoResposta(
                        10L,
                        3498L,
                        criadoEm,
                        "Grand Theft Auto V",
                        LocalDate.of(
                                2013,
                                9,
                                17
                        ),
                        "imagem.jpg",
                        4.47,
                        92,
                        true
                );

        when(
                jogoColecaoService.enriquecer(
                        10L,
                        3498L,
                        criadoEm
                )
        ).thenReturn(
                itemEnriquecido
        );

        mockMvc.perform(
                        get(
                                "/api/usuarios/me/favoritos"
                        )
                                .with(
                                        user(
                                                usuarioPrincipal()
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$[0].rawgGameId")
                                .value(3498)
                )
                .andExpect(
                        jsonPath("$[0].nome")
                                .value(
                                        "Grand Theft Auto V"
                                )
                )
                .andExpect(
                        jsonPath("$[0].dataLancamento")
                                .value(
                                        "2013-09-17"
                                )
                )
                .andExpect(
                        jsonPath("$[0].imagemFundo")
                                .value("imagem.jpg")
                )
                .andExpect(
                        jsonPath("$[0].notaRawg")
                                .value(4.47)
                )
                .andExpect(
                        jsonPath("$[0].metacritic")
                                .value(92)
                )
                .andExpect(
                        jsonPath(
                                "$[0].metadadosDisponiveis"
                        ).value(true)
                );

        verify(favoritoService)
                .listarFavoritos(1L);

        verify(jogoColecaoService)
                .enriquecer(
                        10L,
                        3498L,
                        criadoEm
                );
    }

    @Test
    void deveRemoverFavoritoDoUsuarioAutenticado()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/usuarios/me/favoritos/3498"
                        )
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(favoritoService)
                .removerFavorito(
                        1L,
                        3498L
                );
    }

    @Test
    void deveRetornarConflitoQuandoFavoritoJaExiste()
            throws Exception {

        when(
                favoritoService.adicionarFavorito(
                        1L,
                        3498L
                )
        ).thenThrow(
                new FavoritoJaExisteException(
                        1L,
                        3498L
                )
        );

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Favorito já existente"
                                )
                );
    }

    @Test
    void deveRetornarNaoEncontradoAoRemoverFavoritoInexistente()
            throws Exception {

        doThrow(
                new FavoritoNaoEncontradoException(
                        1L,
                        3498L
                )
        ).when(favoritoService)
                .removerFavorito(
                        1L,
                        3498L
                );

        mockMvc.perform(
                        delete(
                                "/api/usuarios/me/favoritos/3498"
                        )
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isNotFound()
                );
    }

    @Test
    void deveRejeitarRequisicaoSemIdDoJogo()
            throws Exception {

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {}
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                favoritoService
        );
    }

    @Test
    void deveManterFavoritoQuandoMetadadosNaoEstiveremDisponiveis()
            throws Exception {

        Favorito favorito =
                mock(Favorito.class);

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(favorito.getId())
                .thenReturn(10L);

        when(favorito.getRawgGameId())
                .thenReturn(3498L);

        when(favorito.getCreatedAt())
                .thenReturn(criadoEm);

        when(
                favoritoService.listarFavoritos(1L)
        ).thenReturn(
                List.of(favorito)
        );

        when(
                jogoColecaoService.enriquecer(
                        10L,
                        3498L,
                        criadoEm
                )
        ).thenReturn(
                new ItemColecaoJogoResposta(
                        10L,
                        3498L,
                        criadoEm,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                )
        );

        mockMvc.perform(
                        get(
                                "/api/usuarios/me/favoritos"
                        )
                                .with(
                                        user(
                                                usuarioPrincipal()
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[0].rawgGameId")
                                .value(3498)
                )
                .andExpect(
                        jsonPath("$[0].nome")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath(
                                "$[0].metadadosDisponiveis"
                        ).value(false)
                );
    }

    private UsuarioPrincipal usuarioPrincipal() {
        return new UsuarioPrincipal(
                1L,
                "usuario@gamevault.test",
                "{bcrypt}hash"
        );
    }
}
