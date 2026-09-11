package com.gamevault.favorito.integration;

import com.gamevault.favorito.repository.FavoritoRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoritoFluxoIntegracaoTest {

    private static final String EMAIL =
            "usuario.integracao@gamevault.test";

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
        usuario.setEmail(EMAIL);
        usuario.setPassword("senha-hash-teste");

        Usuario usuarioSalvo =
                usuarioRepository.saveAndFlush(usuario);

        usuarioId = usuarioSalvo.getId();
    }

    @Test
    void deveExecutarFluxoCompletoDeFavoritos()
            throws Exception {

        Long rawgGameId = 3498L;

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(usuarioAutenticado())
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
                        jsonPath("$.rawgGameId")
                                .value(3498)
                );

        assertTrue(
                favoritoRepository
                        .existsByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
        );

        mockMvc.perform(
                        get("/api/usuarios/me/favoritos")
                                .with(usuarioAutenticado())
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].rawgGameId")
                                .value(3498)
                );

        mockMvc.perform(
                        delete(
                                "/api/usuarios/me/favoritos/{rawgGameId}",
                                rawgGameId
                        )
                                .with(usuarioAutenticado())
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        assertFalse(
                favoritoRepository
                        .existsByUsuarioIdAndRawgGameId(
                                usuarioId,
                                rawgGameId
                        )
        );
    }

    @Test
    void deveRetornarConflitoAoAdicionarMesmoJogoDuasVezes()
            throws Exception {

        String corpo = """
                {
                  "rawgGameId": 3498
                }
                """;

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(usuarioAutenticado())
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(corpo)
                )
                .andExpect(
                        status().isCreated()
                );

        mockMvc.perform(
                        post("/api/usuarios/me/favoritos")
                                .with(usuarioAutenticado())
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(corpo)
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
    void deveAssociarFavoritoSomenteAoUsuarioAutenticado()
            throws Exception {

        Usuario outroUsuario = new Usuario();
        outroUsuario.setName("Outro Usuario");
        outroUsuario.setUsername("outro_usuario");
        outroUsuario.setEmail(
                "outro.usuario@gamevault.test"
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
                        post("/api/usuarios/me/favoritos")
                                .with(usuarioAutenticado())
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

        assertFalse(
                favoritoRepository
                        .existsByUsuarioIdAndRawgGameId(
                                outroUsuarioId,
                                rawgGameId
                        )
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
