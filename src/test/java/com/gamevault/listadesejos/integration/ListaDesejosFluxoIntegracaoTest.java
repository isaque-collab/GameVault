package com.gamevault.listadesejos.integration;

import com.gamevault.user.security.UsuarioPrincipal;
import com.gamevault.favorito.repository.FavoritoRepository;
import com.gamevault.listadesejos.repository.ItemListaDesejosRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ListaDesejosFluxoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ItemListaDesejosRepository itemListaDesejosRepository;

    @Autowired
    private FavoritoRepository favoritoRepository;

    private Long usuarioId;

    @BeforeEach
    void prepararUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Usuário Integração Wishlist");
        usuario.setUsername("usuario_integracao_wishlist");
        usuario.setEmail("usuario.integracao.wishlist@gamevault.test");
        usuario.setPassword("senha-hash-teste");

        Usuario usuarioSalvo =
                usuarioRepository.saveAndFlush(usuario);

        usuarioId = usuarioSalvo.getId();
    }

    @Test
    @WithMockUser
    void deveExecutarFluxoCompletoDaListaDeDesejos()
            throws Exception {

        Long rawgGameId = 3498L;

        // ADICIONAR
        mockMvc.perform(
                        post(
                                "/api/usuarios/{usuarioId}/lista-desejos",
                                usuarioId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "rawgGameId": 3498
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.rawgGameId").value(3498)
                );

        assertTrue(
                itemListaDesejosRepository
                        .existsByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
        );

        // LISTAR
        mockMvc.perform(
                        get(
                                "/api/usuarios/{usuarioId}/lista-desejos",
                                usuarioId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].rawgGameId").value(3498)
                );

        // REMOVER
        mockMvc.perform(
                        delete(
                                "/api/usuarios/{usuarioId}/lista-desejos/{rawgGameId}",
                                usuarioId,
                                rawgGameId
                        )
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        assertFalse(
                itemListaDesejosRepository
                        .existsByUsuarioIdAndRawgGameId(
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
                        post(
                                "/api/usuarios/{usuarioId}/lista-desejos",
                                usuarioId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post(
                                "/api/usuarios/{usuarioId}/lista-desejos",
                                usuarioId
                        )
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(corpo)
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
    void deveRetornarNaoEncontradoParaUsuarioInexistente()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/usuarios/{usuarioId}/lista-desejos",
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

    @Test
    void devePermitirMesmoJogoNosFavoritosENaListaDeDesejos()
            throws Exception {

        Long rawgGameId = 3498L;

        UsuarioPrincipal usuarioPrincipal =
                new UsuarioPrincipal(
                        usuarioId,
                        "usuario.integracao.wishlist@gamevault.test",
                        "senha-hash-teste"
                );

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(user(usuarioPrincipal))
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
                );

        mockMvc.perform(
                        post(
                                "/api/usuarios/{usuarioId}/lista-desejos",
                                usuarioId
                        )
                                .with(user(usuarioPrincipal))
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
                );

        assertTrue(
                favoritoRepository
                        .existsByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
        );

        assertTrue(
                itemListaDesejosRepository
                        .existsByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
        );
    }
}
