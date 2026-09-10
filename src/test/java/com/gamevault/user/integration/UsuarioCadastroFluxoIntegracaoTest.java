package com.gamevault.user.integration;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioCadastroFluxoIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    void deveCadastrarUsuarioComSenhaProtegida()
            throws Exception {

        String senhaOriginal = "senha123";

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Usuário Integração",
                                          "username": "usuario_integracao_cadastro",
                                          "email": "usuario.integracao.cadastro@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.nome")
                                .value("Usuário Integração")
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("usuario_integracao_cadastro")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "usuario.integracao.cadastro@gamevault.test"
                                )
                )
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(
                        jsonPath("$.confirmacaoSenha").doesNotExist()
                );

        Usuario usuarioSalvo = usuarioRepository
                .findAll()
                .stream()
                .filter(
                        usuario -> usuario
                                .getUsername()
                                .equals(
                                        "usuario_integracao_cadastro"
                                )
                )
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertNotNull(usuarioSalvo.getId()),

                () -> assertNotEquals(
                        senhaOriginal,
                        usuarioSalvo.getPassword()
                ),

                () -> assertTrue(
                        passwordEncoder.matches(
                                senhaOriginal,
                                usuarioSalvo.getPassword()
                        )
                )
        );
    }

    @Test
    @WithMockUser
    void deveImpedirCadastroComUsernameDuplicado()
            throws Exception {

        String primeiroCadastro = """
                {
                  "nome": "Primeiro Usuário",
                  "username": "username_duplicado",
                  "email": "primeiro.usuario@gamevault.test",
                  "senha": "senha123",
                  "confirmacaoSenha": "senha123"
                }
                """;

        String segundoCadastro = """
                {
                  "nome": "Segundo Usuário",
                  "username": "username_duplicado",
                  "email": "segundo.usuario@gamevault.test",
                  "senha": "senha456",
                  "confirmacaoSenha": "senha456"
                }
                """;

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primeiroCadastro)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segundoCadastro)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Dados de usuário já cadastrados"
                                )
                );

        assertTrue(
                usuarioRepository.existsByUsername(
                        "username_duplicado"
                )
        );
    }

    @Test
    @WithMockUser
    void deveImpedirCadastroComEmailDuplicado()
            throws Exception {

        String primeiroCadastro = """
                {
                  "nome": "Primeiro Usuário",
                  "username": "primeiro_usuario_email",
                  "email": "email.duplicado@gamevault.test",
                  "senha": "senha123",
                  "confirmacaoSenha": "senha123"
                }
                """;

        String segundoCadastro = """
                {
                  "nome": "Segundo Usuário",
                  "username": "segundo_usuario_email",
                  "email": "email.duplicado@gamevault.test",
                  "senha": "senha456",
                  "confirmacaoSenha": "senha456"
                }
                """;

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primeiroCadastro)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segundoCadastro)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Dados de usuário já cadastrados"
                                )
                );

        assertTrue(
                usuarioRepository.existsByEmail(
                        "email.duplicado@gamevault.test"
                )
        );
    }
}
