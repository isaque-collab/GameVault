package com.gamevault.avaliacao.integration;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.repository.AvaliacaoRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import com.gamevault.user.security.UsuarioPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvaliacaoFluxoIntegracaoTest {

    private static final String EMAIL =
            "usuario.integracao.avaliacao@gamevault.test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Long usuarioId;

    @BeforeEach
    void prepararUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName(
                "Usuário Integração Avaliação"
        );
        usuario.setUsername(
                "usuario_integracao_avaliacao"
        );
        usuario.setEmail(EMAIL);
        usuario.setPassword(
                "senha-hash-teste"
        );

        Usuario usuarioSalvo =
                usuarioRepository.saveAndFlush(usuario);

        usuarioId = usuarioSalvo.getId();
    }

    @Test
    void deveExecutarFluxoCompletoDeAvaliacaoSemDuplicarAoAtualizar()
            throws Exception {

        Long rawgGameId = 987654321L;

        // JOGO AINDA SEM AVALIAÇÕES
        mockMvc.perform(
                        get(
                                "/api/jogos/{rawgGameId}/avaliacoes/resumo",
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.media")
                                .value(nullValue())
                )
                .andExpect(
                        jsonPath("$.quantidade")
                                .value(0)
                );

        // USUÁRIO AUTENTICADO AVALIA COM NOTA 3
        mockMvc.perform(
                        put(
                                "/api/usuarios/me/avaliacoes/{rawgGameId}",
                                rawgGameId
                        )
                                .with(usuarioAutenticado())
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "nota": 3
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.rawgGameId")
                                .value(rawgGameId)
                )
                .andExpect(
                        jsonPath("$.nota")
                                .value(3)
                );

        Avaliacao avaliacaoInicial =
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
                        .orElseThrow();

        Long avaliacaoId =
                avaliacaoInicial.getId();

        assertEquals(
                (byte) 3,
                avaliacaoInicial.getRating()
        );

        assertEquals(
                1L,
                avaliacaoRepository
                        .countByRawgGameId(
                                rawgGameId
                        )
        );

        // MESMO USUÁRIO ALTERA A NOTA PARA 5
        mockMvc.perform(
                        put(
                                "/api/usuarios/me/avaliacoes/{rawgGameId}",
                                rawgGameId
                        )
                                .with(usuarioAutenticado())
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "nota": 5
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.nota")
                                .value(5)
                );

        Avaliacao avaliacaoAtualizada =
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
                        .orElseThrow();

        assertAll(
                () -> assertEquals(
                        avaliacaoId,
                        avaliacaoAtualizada.getId()
                ),
                () -> assertEquals(
                        (byte) 5,
                        avaliacaoAtualizada.getRating()
                ),
                () -> assertEquals(
                        1L,
                        avaliacaoRepository
                                .countByRawgGameId(
                                        rawgGameId
                                )
                )
        );

        // CONSULTA A PRÓPRIA AVALIAÇÃO
        mockMvc.perform(
                        get(
                                "/api/usuarios/me/avaliacoes/{rawgGameId}",
                                rawgGameId
                        )
                                .with(usuarioAutenticado())
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.rawgGameId")
                                .value(rawgGameId)
                )
                .andExpect(
                        jsonPath("$.nota")
                                .value(5)
                );

        // RESUMO PÚBLICO DA COMUNIDADE
        mockMvc.perform(
                        get(
                                "/api/jogos/{rawgGameId}/avaliacoes/resumo",
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.media")
                                .value(5.0)
                )
                .andExpect(
                        jsonPath("$.quantidade")
                                .value(1)
                );

        // USUÁRIO REMOVE A PRÓPRIA AVALIAÇÃO
        mockMvc.perform(
                        delete(
                                "/api/usuarios/me/avaliacoes/{rawgGameId}",
                                rawgGameId
                        )
                                .with(usuarioAutenticado())
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        assertTrue(
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
                        .isEmpty()
        );

        // JOGO VOLTA A FICAR SEM AVALIAÇÕES
        mockMvc.perform(
                        get(
                                "/api/jogos/{rawgGameId}/avaliacoes/resumo",
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.media")
                                .value(nullValue())
                )
                .andExpect(
                        jsonPath("$.quantidade")
                                .value(0)
                );
    }

    @Test
    void deveAssociarAvaliacaoSomenteAoUsuarioAutenticado()
            throws Exception {

        Usuario outroUsuario =
                new Usuario();

        outroUsuario.setName(
                "Outro Usuário Avaliação"
        );
        outroUsuario.setUsername(
                "outro_usuario_avaliacao"
        );
        outroUsuario.setEmail(
                "outro.usuario.avaliacao@gamevault.test"
        );
        outroUsuario.setPassword(
                "senha-hash-teste"
        );

        Long outroUsuarioId =
                usuarioRepository
                        .saveAndFlush(outroUsuario)
                        .getId();

        Long rawgGameId = 3498L;

        mockMvc.perform(
                        put(
                                "/api/usuarios/me/avaliacoes/{rawgGameId}",
                                rawgGameId
                        )
                                .with(usuarioAutenticado())
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "nota": 4
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.rawgGameId")
                                .value(rawgGameId)
                )
                .andExpect(
                        jsonPath("$.nota")
                                .value(4)
                );

        assertTrue(
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
                        .isPresent()
        );

        assertTrue(
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                outroUsuarioId,
                                rawgGameId
                        )
                        .isEmpty()
        );
    }

    private RequestPostProcessor usuarioAutenticado() {

        UsuarioPrincipal principal =
                new UsuarioPrincipal(
                        usuarioId,
                        EMAIL,
                        "senha-hash-teste"
                );

        return user(principal);
    }
}