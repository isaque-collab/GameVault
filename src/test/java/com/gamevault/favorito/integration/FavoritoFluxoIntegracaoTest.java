package com.gamevault.favorito.integration;

import com.gamevault.favorito.repository.FavoritoRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoritoFluxoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    private Long usuarioId;

    @BeforeEach
    void prepararUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Usuário Integração");
        usuario.setUsername("usuario_integracao");
        usuario.setEmail("usuario.integracao@gamevault.test");
        usuario.setPassword("senha-hash-teste");

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        usuarioId = usuarioSalvo.getId();
    }

    @Test
    @WithMockUser
    void deveExecutarFluxoCompletoDeFavoritos() throws Exception {

        Long rawgGameId = 3498L;

        // ADICIONAR
        mockMvc.perform(
                        post("/api/usuarios/{usuarioId}/favoritos", usuarioId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rawgGameId").value(3498));

        assertTrue(
                favoritoRepository.existsByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        );

        // LISTAR
        mockMvc.perform(
                        get("/api/usuarios/{usuarioId}/favoritos", usuarioId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rawgGameId").value(3498));

        // REMOVER
        mockMvc.perform(
                        delete(
                                "/api/usuarios/{usuarioId}/favoritos/{rawgGameId}",
                                usuarioId,
                                rawgGameId
                        ).with(csrf())
                )
                .andExpect(status().isNoContent());

        assertFalse(
                favoritoRepository.existsByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                )
        );
    }

    @Test
    @WithMockUser
    void deveRetornarConflitoAoAdicionarMesmoJogoDuasVezes()
            throws Exception {

        String corpo = """
                {
                  "rawgGameId": 3498
                }
                """;

        mockMvc.perform(
                        post("/api/usuarios/{usuarioId}/favoritos", usuarioId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/usuarios/{usuarioId}/favoritos", usuarioId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.title")
                                .value("Favorito já existente")
                );
    }

    @Test
    @WithMockUser
    void deveRetornarNaoEncontradoParaUsuarioInexistente()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/usuarios/{usuarioId}/favoritos",
                                Long.MAX_VALUE
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.title")
                                .value("Recurso não encontrado")
                );
    }
}
