package com.gamevault.avaliacao.integration;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.repository.AvaliacaoRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvaliacaoFluxoIntegracaoTest {

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
        usuario.setName("Usuário Integração Avaliação");
        usuario.setUsername("usuario_integracao_avaliacao");
        usuario.setEmail("usuario.integracao.avaliacao@gamevault.test");
        usuario.setPassword("senha-hash-teste");

        Usuario usuarioSalvo =
                usuarioRepository.saveAndFlush(usuario);

        usuarioId = usuarioSalvo.getId();
    }

    @Test
    @WithMockUser
    void deveExecutarFluxoCompletoDeAvaliacaoSemDuplicarAoAtualizar()
            throws Exception {

        Long rawgGameId = 987654321L;

        // SEM AVALIAÇÕES
        mockMvc.perform(
                        get(
                                "/api/jogos/{rawgGameId}/avaliacoes/resumo",
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(nullValue()))
                .andExpect(jsonPath("$.quantidade").value(0));

        // AVALIAR COM NOTA 3
        mockMvc.perform(
                        put(
                                "/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}",
                                usuarioId,
                                rawgGameId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
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
                .andExpect(jsonPath("$.nota").value(3));

        Avaliacao avaliacaoInicial = avaliacaoRepository
                .findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
                .orElseThrow();

        Long avaliacaoId = avaliacaoInicial.getId();

        assertEquals(
                (byte) 3,
                avaliacaoInicial.getRating()
        );

        assertEquals(
                1L,
                avaliacaoRepository.countByRawgGameId(rawgGameId)
        );

        // ALTERAR NOTA PARA 5
        mockMvc.perform(
                        put(
                                "/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}",
                                usuarioId,
                                rawgGameId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nota": 5
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(5));

        Avaliacao avaliacaoAtualizada = avaliacaoRepository
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
                        avaliacaoRepository.countByRawgGameId(
                                rawgGameId
                        )
                )
        );

        // CONSULTAR AVALIAÇÃO DO USUÁRIO
        mockMvc.perform(
                        get(
                                "/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}",
                                usuarioId,
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.rawgGameId")
                                .value(rawgGameId)
                )
                .andExpect(jsonPath("$.nota").value(5));

        // RESUMO DA COMUNIDADE
        mockMvc.perform(
                        get(
                                "/api/jogos/{rawgGameId}/avaliacoes/resumo",
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(5.0))
                .andExpect(jsonPath("$.quantidade").value(1));

        // REMOVER
        mockMvc.perform(
                        delete(
                                "/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}",
                                usuarioId,
                                rawgGameId
                        )
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        assertTrue(
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
                        .isEmpty()
        );

        // VOLTA A FICAR SEM AVALIAÇÕES
        mockMvc.perform(
                        get(
                                "/api/jogos/{rawgGameId}/avaliacoes/resumo",
                                rawgGameId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.media").value(nullValue()))
                .andExpect(jsonPath("$.quantidade").value(0));
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoAoAvaliarComUsuarioInexistente()
            throws Exception {

        Long rawgGameId = 987654321L;

        mockMvc.perform(
                        put(
                                "/api/usuarios/{usuarioId}/avaliacoes/{rawgGameId}",
                                Long.MAX_VALUE,
                                rawgGameId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nota": 5
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.title")
                                .value("Recurso não encontrado")
                );

        assertTrue(
                avaliacaoRepository
                        .findByUsuarioIdAndRawgGameId(
                                Long.MAX_VALUE,
                                rawgGameId
                        )
                        .isEmpty()
        );
    }
}
