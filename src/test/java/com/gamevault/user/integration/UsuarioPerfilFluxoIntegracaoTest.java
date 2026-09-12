package com.gamevault.user.integration;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioPerfilFluxoIntegracaoTest {

    private static final String EMAIL =
            "usuario.perfil@gamevault.test";

    private static final String SENHA =
            "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Usuário Perfil");
        usuario.setUsername("usuario_perfil");
        usuario.setEmail(EMAIL);
        usuario.setPassword(
                passwordEncoder.encode(SENHA)
        );

        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void deveBuscarPerfilDoUsuarioAutenticado()
            throws Exception {

        MockHttpSession sessao =
                autenticar();

        mockMvc.perform(
                        get("/api/usuarios/me")
                                .session(sessao)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.nome")
                                .value("Usuário Perfil")
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("usuario_perfil")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(EMAIL)
                )
                .andExpect(
                        jsonPath("$.imagemPerfil")
                                .isEmpty()
                )
                .andExpect(
                        jsonPath("$.senha")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.password")
                                .doesNotExist()
                );
    }

    @Test
    void deveBloquearConsultaDePerfilSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/usuarios/me")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    private MockHttpSession autenticar()
            throws Exception {

        MvcResult resultado =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .with(csrf())
                                        .contentType(
                                                MediaType.APPLICATION_FORM_URLENCODED
                                        )
                                        .param(
                                                "email",
                                                EMAIL
                                        )
                                        .param(
                                                "senha",
                                                SENHA
                                        )
                        )
                        .andExpect(
                                status().isNoContent()
                        )
                        .andReturn();

        MockHttpSession sessao =
                (MockHttpSession)
                        resultado
                                .getRequest()
                                .getSession(false);

        assertNotNull(sessao);

        return sessao;
    }
}