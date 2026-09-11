package com.gamevault.user.integration;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutenticacaoFluxoIntegracaoTest {

    private static final String EMAIL =
            "usuario.auth@gamevault.test";

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
        usuario.setName("Usuario Autenticacao");
        usuario.setUsername("usuario_auth");
        usuario.setEmail(EMAIL);
        usuario.setPassword(
                passwordEncoder.encode(SENHA)
        );

        usuarioRepository.saveAndFlush(usuario);
    }

    @Test
    void devePermitirObterTokenCsrfSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/csrf")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .isNotEmpty()
                );
    }

    @Test
    void devePermitirCadastroSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "nome": "Novo Usuario",
                                          "username": "novo_usuario",
                                          "email": "novo.usuario@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(
                        status().isCreated()
                );
    }

    @Test
    void deveBloquearRecursoProtegidoSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/usuarios/me/favoritos")
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void deveAutenticarUsuarioEManterSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticar();

        mockMvc.perform(
                        get(
                                "/api/usuarios/me/favoritos")
                                .session(sessao)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        authenticated()
                                .withUsername(EMAIL)
                );
    }

    @Test
    void deveRejeitarLoginComSenhaInvalida()
            throws Exception {

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
                                        "senha-incorreta"
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void deveExigirCsrfNoLogin()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
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
                        status().isForbidden()
                );
    }

    @Test
    void deveInvalidarSessaoNoLogout()
            throws Exception {

        MockHttpSession sessao =
                autenticar();

        mockMvc.perform(
                        post("/api/auth/logout")
                                .session(sessao)
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        assertTrue(
                sessao.isInvalid()
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
                        .andExpect(
                                authenticated()
                                        .withUsername(EMAIL)
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
