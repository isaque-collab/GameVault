package com.gamevault.listadesejos.controller;

import com.gamevault.jogo.dto.ItemColecaoJogoResposta;
import com.gamevault.jogo.service.JogoColecaoService;
import com.gamevault.listadesejos.entity.ItemListaDesejos;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.listadesejos.service.ListaDesejosService;
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

@WebMvcTest(ListaDesejosController.class)
@Import(TratadorGlobalExcecoes.class)
class ListaDesejosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListaDesejosService listaDesejosService;

    @MockitoBean
    private JogoColecaoService jogoColecaoService;

    @Test
    void deveAdicionarJogoNaListaDeDesejos()
            throws Exception {

        ItemListaDesejos item =
                mock(ItemListaDesejos.class);

        when(item.getId())
                .thenReturn(1L);

        when(item.getRawgGameId())
                .thenReturn(3498L);

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

        when(
                listaDesejosService.adicionar(
                        1L,
                        3498L
                )
        ).thenReturn(item);

        mockMvc.perform(
                        post("/api/usuarios/me/lista-desejos")
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
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.rawgGameId")
                                .value(3498)
                );

        verify(listaDesejosService)
                .adicionar(
                        1L,
                        3498L
                );
    }

    @Test
    void deveListarJogosDaListaDeDesejosDoUsuarioAutenticado()
            throws Exception {

        ItemListaDesejos item =
                mock(ItemListaDesejos.class);

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(item.getId())
                .thenReturn(20L);

        when(item.getRawgGameId())
                .thenReturn(3498L);

        when(item.getCreatedAt())
                .thenReturn(criadoEm);

        when(
                listaDesejosService.listar(1L)
        ).thenReturn(
                List.of(item)
        );

        when(
                jogoColecaoService.enriquecer(
                        20L,
                        3498L,
                        criadoEm
                )
        ).thenReturn(
                new ItemColecaoJogoResposta(
                        20L,
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
                )
        );

        mockMvc.perform(
                        get(
                                "/api/usuarios/me/lista-desejos"
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
                                .value(20)
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
                        jsonPath("$[0].imagemFundo")
                                .value("imagem.jpg")
                )
                .andExpect(
                        jsonPath(
                                "$[0].metadadosDisponiveis"
                        ).value(true)
                );

        verify(listaDesejosService)
                .listar(1L);

        verify(jogoColecaoService)
                .enriquecer(
                        20L,
                        3498L,
                        criadoEm
                );
    }

    @Test
    void deveRemoverJogoDaListaDeDesejosDoUsuarioAutenticado()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/usuarios/me/lista-desejos/3498"
                        )
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(listaDesejosService)
                .remover(
                        1L,
                        3498L
                );
    }

    @Test
    void deveRetornarConflitoQuandoJogoJaEstaNaListaDeDesejos()
            throws Exception {

        when(
                listaDesejosService.adicionar(
                        1L,
                        3498L
                )
        ).thenThrow(
                new ItemListaDesejosJaExisteException(
                        1L,
                        3498L
                )
        );

        mockMvc.perform(
                        post("/api/usuarios/me/lista-desejos")
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
                                        "Item já existente na lista de desejos"
                                )
                );
    }

    @Test
    void deveRetornarNaoEncontradoAoRemoverJogoQueNaoEstaNaListaDeDesejos()
            throws Exception {

        doThrow(
                new ItemListaDesejosNaoEncontradoException(
                        1L,
                        3498L
                )
        ).when(listaDesejosService)
                .remover(
                        1L,
                        3498L
                );

        mockMvc.perform(
                        delete(
                                "/api/usuarios/me/lista-desejos/3498"
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
                        post("/api/usuarios/me/lista-desejos")
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
                listaDesejosService
        );
    }

    @Test
    void deveManterItemDaListaDeDesejosQuandoMetadadosNaoEstiveremDisponiveis()
            throws Exception {

        ItemListaDesejos item =
                mock(ItemListaDesejos.class);

        LocalDateTime criadoEm =
                LocalDateTime.of(
                        2026,
                        9,
                        21,
                        20,
                        0
                );

        when(item.getId())
                .thenReturn(20L);

        when(item.getRawgGameId())
                .thenReturn(3498L);

        when(item.getCreatedAt())
                .thenReturn(criadoEm);

        when(
                listaDesejosService.listar(1L)
        ).thenReturn(
                List.of(item)
        );

        when(
                jogoColecaoService.enriquecer(
                        20L,
                        3498L,
                        criadoEm
                )
        ).thenReturn(
                new ItemColecaoJogoResposta(
                        20L,
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
                                "/api/usuarios/me/lista-desejos"
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