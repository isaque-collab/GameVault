package com.gamevault.user.controller;

import com.gamevault.shared.exception.TratadorGlobalExcecoes;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.*;
import com.gamevault.user.security.UsuarioPrincipal;
import com.gamevault.user.service.FotoPerfilService;
import com.gamevault.user.service.UsuarioService;
import com.gamevault.user.storage.FotoPerfilArquivo;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(UsuarioController.class)
@Import(TratadorGlobalExcecoes.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private FotoPerfilService fotoPerfilService;

    @Test
    @WithMockUser
    void deveCadastrarUsuario() throws Exception {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId()).thenReturn(1L);
        when(usuario.getName()).thenReturn("Isaque Costa");
        when(usuario.getUsername()).thenReturn("isaque");
        when(usuario.getEmail())
                .thenReturn("isaque@gamevault.test");
        when(usuario.getProfileImageUrl()).thenReturn(null);

        when(
                usuarioService.cadastrar(
                        "Isaque Costa",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha123"
                )
        ).thenReturn(usuario);

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(
                        jsonPath("$.nome")
                                .value("Isaque Costa")
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("isaque")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("isaque@gamevault.test")
                )
                .andExpect(
                        jsonPath("$.imagemPerfil").value(nullValue())
                )
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(
                        jsonPath("$.confirmacaoSenha").doesNotExist()
                );

        verify(usuarioService).cadastrar(
                "Isaque Costa",
                "isaque",
                "isaque@gamevault.test",
                "senha123",
                "senha123"
        );
    }

    @Test
    @WithMockUser
    void deveRejeitarCadastroComNomeVazio() throws Exception {
        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @WithMockUser
    void deveRejeitarCadastroComEmailInvalido() throws Exception {
        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "email-invalido",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @WithMockUser
    void deveRejeitarCadastroComSenhaCurta() throws Exception {
        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "1234567",
                                          "confirmacaoSenha": "1234567"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @WithMockUser
    void deveRejeitarCadastroSemConfirmacaoDeSenha()
            throws Exception {

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @WithMockUser
    void deveRetornarConflitoQuandoUsernameJaEstiverCadastrado()
            throws Exception {

        when(
                usuarioService.cadastrar(
                        "Isaque Costa",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha123"
                )
        ).thenThrow(
                new UsernameJaCadastradoException("isaque")
        );

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Dados de usuário já cadastrados"
                                )
                );
    }

    @Test
    @WithMockUser
    void deveRetornarConflitoQuandoEmailJaEstiverCadastrado()
            throws Exception {

        when(
                usuarioService.cadastrar(
                        "Isaque Costa",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha123"
                )
        ).thenThrow(
                new EmailJaCadastradoException(
                        "isaque@gamevault.test"
                )
        );

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha123"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Dados de usuário já cadastrados"
                                )
                );
    }

    @Test
    @WithMockUser
    void deveRetornarErroQuandoSenhasNaoCoincidirem()
            throws Exception {

        when(
                usuarioService.cadastrar(
                        "Isaque Costa",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha456"
                )
        ).thenThrow(
                new SenhasNaoCoincidemException()
        );

        mockMvc.perform(
                        post("/api/usuarios")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "nome": "Isaque Costa",
                                          "username": "isaque",
                                          "email": "isaque@gamevault.test",
                                          "senha": "senha123",
                                          "confirmacaoSenha": "senha456"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.title")
                                .value("Senha inválida")
                );
    }

    @Test
    void deveBuscarPerfilDoUsuarioAutenticado()
            throws Exception {

        Usuario usuario = mock(Usuario.class);

        when(usuario.getId())
                .thenReturn(1L);

        when(usuario.getName())
                .thenReturn("Isaque Costa");

        when(usuario.getUsername())
                .thenReturn("isaque");

        when(usuario.getEmail())
                .thenReturn("isaque@gamevault.test");

        when(usuario.getProfileImageUrl())
                .thenReturn(null);

        when(
                usuarioService.buscarPorId(1L)
        ).thenReturn(usuario);

        mockMvc.perform(
                        get("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.nome")
                                .value("Isaque Costa")
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("isaque")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("isaque@gamevault.test")
                )
                .andExpect(
                        jsonPath("$.imagemPerfil")
                                .value(nullValue())
                )
                .andExpect(
                        jsonPath("$.senha")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.password")
                                .doesNotExist()
                );

        verify(usuarioService)
                .buscarPorId(1L);
    }

    @Test
    void deveAtualizarPerfilDoUsuarioAutenticado()
            throws Exception {

        Usuario usuario = mock(Usuario.class);

        when(usuario.getId())
                .thenReturn(1L);

        when(usuario.getName())
                .thenReturn("Isaque Costa da Cunha");

        when(usuario.getUsername())
                .thenReturn("isaque-collab");

        when(usuario.getEmail())
                .thenReturn("isaque@gamevault.test");

        when(usuario.getProfileImageUrl())
                .thenReturn(null);

        when(
                usuarioService.atualizarPerfil(
                        1L,
                        "Isaque Costa da Cunha",
                        "isaque-collab",
                        "isaque@gamevault.test"
                )
        ).thenReturn(usuario);

        mockMvc.perform(
                        put("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "nome": "Isaque Costa da Cunha",
                                      "username": "isaque-collab",
                                      "email": "isaque@gamevault.test"
                                    }
                                    """)
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.nome")
                                .value(
                                        "Isaque Costa da Cunha"
                                )
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("isaque-collab")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "isaque@gamevault.test"
                                )
                )
                .andExpect(
                        jsonPath("$.imagemPerfil")
                                .value(nullValue())
                )
                .andExpect(
                        jsonPath("$.senha")
                                .doesNotExist()
                )
                .andExpect(
                        jsonPath("$.password")
                                .doesNotExist()
                );

        verify(usuarioService)
                .atualizarPerfil(
                        1L,
                        "Isaque Costa da Cunha",
                        "isaque-collab",
                        "isaque@gamevault.test"
                );
    }

    @Test
    void deveRejeitarAtualizacaoComNomeVazio()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "nome": "",
                                      "username": "isaque",
                                      "email": "isaque@gamevault.test"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                usuarioService
        );
    }

    @Test
    void deveRejeitarAtualizacaoComEmailInvalido()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "nome": "Isaque Costa",
                                      "username": "isaque",
                                      "email": "email-invalido"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                usuarioService
        );
    }

    @Test
    void deveRejeitarAtualizacaoComUsernameVazio()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "nome": "Isaque Costa",
                                      "username": "",
                                      "email": "isaque@gamevault.test"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                usuarioService
        );
    }

    @Test
    void deveRetornarConflitoAoAtualizarParaUsernameJaCadastrado()
            throws Exception {

        when(
                usuarioService.atualizarPerfil(
                        1L,
                        "Isaque Costa",
                        "username_existente",
                        "isaque@gamevault.test"
                )
        ).thenThrow(
                new UsernameJaCadastradoException(
                        "username_existente"
                )
        );

        mockMvc.perform(
                        put("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "nome": "Isaque Costa",
                                      "username": "username_existente",
                                      "email": "isaque@gamevault.test"
                                    }
                                    """)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Dados de usuário já cadastrados"
                                )
                );
    }

    @Test
    void deveRetornarConflitoAoAtualizarParaEmailJaCadastrado()
            throws Exception {

        when(
                usuarioService.atualizarPerfil(
                        1L,
                        "Isaque Costa",
                        "isaque",
                        "email.existente@gamevault.test"
                )
        ).thenThrow(
                new EmailJaCadastradoException(
                        "email.existente@gamevault.test"
                )
        );

        mockMvc.perform(
                        put("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "nome": "Isaque Costa",
                                      "username": "isaque",
                                      "email": "email.existente@gamevault.test"
                                    }
                                    """)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Dados de usuário já cadastrados"
                                )
                );
    }

    @Test
    void deveAlterarSenhaDoUsuarioAutenticado()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me/senha")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "senhaAtual": "senhaAtual123",
                                      "novaSenha": "novaSenha123",
                                      "confirmacaoNovaSenha": "novaSenha123"
                                    }
                                    """)
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(usuarioService)
                .alterarSenha(
                        1L,
                        "senhaAtual123",
                        "novaSenha123",
                        "novaSenha123"
                );
    }

    @Test
    void deveRetornarErroQuandoSenhaAtualEstiverIncorreta()
            throws Exception {

        doThrow(
                new SenhaAtualInvalidaException()
        ).when(usuarioService)
                .alterarSenha(
                        1L,
                        "senhaErrada",
                        "novaSenha123",
                        "novaSenha123"
                );

        mockMvc.perform(
                        put("/api/usuarios/me/senha")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "senhaAtual": "senhaErrada",
                                      "novaSenha": "novaSenha123",
                                      "confirmacaoNovaSenha": "novaSenha123"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value("Senha inválida")
                );
    }

    @Test
    void deveRejeitarNovaSenhaCurta()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me/senha")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "senhaAtual": "senhaAtual123",
                                      "novaSenha": "1234567",
                                      "confirmacaoNovaSenha": "1234567"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                usuarioService
        );
    }

    @Test
    void deveRejeitarAlteracaoSemSenhaAtual()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me/senha")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "novaSenha": "novaSenha123",
                                      "confirmacaoNovaSenha": "novaSenha123"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                usuarioService
        );
    }

    @Test
    void deveRejeitarAlteracaoSemConfirmacaoDaNovaSenha()
            throws Exception {

        mockMvc.perform(
                        put("/api/usuarios/me/senha")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "senhaAtual": "senhaAtual123",
                                      "novaSenha": "novaSenha123"
                                    }
                                    """)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                usuarioService
        );
    }

    @Test
    void deveExcluirContaDoUsuarioAutenticado()
            throws Exception {

        mockMvc.perform(
                        delete("/api/usuarios/me")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        InOrder ordem =
                inOrder(
                        fotoPerfilService,
                        usuarioService
                );

        ordem.verify(fotoPerfilService)
                .removerFoto(1L);

        ordem.verify(usuarioService)
                .excluirConta(1L);
    }

    @Test
    void deveDefinirFotoDePerfilDoUsuarioAutenticado()
            throws Exception {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        new byte[]{
                                (byte) 0xFF,
                                (byte) 0xD8,
                                (byte) 0xFF
                        }
                );

        Usuario usuario = mock(Usuario.class);

        when(usuario.getId())
                .thenReturn(1L);

        when(usuario.getName())
                .thenReturn("Isaque");

        when(usuario.getUsername())
                .thenReturn("isaque");

        when(usuario.getEmail())
                .thenReturn("isaque@gamevault.test");

        when(usuario.getProfileImageUrl())
                .thenReturn("foto-interna.jpg");

        when(
                fotoPerfilService.atualizarFoto(
                        eq(1L),
                        any(MockMultipartFile.class)
                )
        ).thenReturn(usuario);

        mockMvc.perform(
                        multipart(
                                HttpMethod.PUT,
                                "/api/usuarios/me/foto"
                        )
                                .file(foto)
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.imagemPerfil")
                                .value(
                                        "/api/usuarios/me/foto"
                                )
                );

        verify(fotoPerfilService)
                .atualizarFoto(
                        eq(1L),
                        any(MockMultipartFile.class)
                );
    }

    @Test
    void deveRetornarFotoDePerfilDoUsuarioAutenticado()
            throws Exception {

        byte[] conteudo = {
                (byte) 0xFF,
                (byte) 0xD8,
                (byte) 0xFF,
                0x01
        };

        FotoPerfilArquivo arquivo =
                new FotoPerfilArquivo(
                        conteudo,
                        MediaType.IMAGE_JPEG
                );

        when(
                fotoPerfilService.buscarFoto(1L)
        ).thenReturn(
                arquivo
        );

        mockMvc.perform(
                        get("/api/usuarios/me/foto")
                                .with(user(usuarioPrincipal()))
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().contentType(
                                MediaType.IMAGE_JPEG
                        )
                )
                .andExpect(
                        content().bytes(
                                conteudo
                        )
                );

        verify(fotoPerfilService)
                .buscarFoto(1L);
    }

    @Test
    void deveRemoverFotoDePerfilDoUsuarioAutenticado()
            throws Exception {

        Usuario usuario = mock(Usuario.class);

        when(
                fotoPerfilService.removerFoto(1L)
        ).thenReturn(
                usuario
        );

        mockMvc.perform(
                        delete("/api/usuarios/me/foto")
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(fotoPerfilService)
                .removerFoto(1L);
    }

    @Test
    void deveBloquearUploadDeFotoSemAutenticacao()
            throws Exception {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        new byte[]{
                                (byte) 0xFF,
                                (byte) 0xD8,
                                (byte) 0xFF
                        }
                );

        mockMvc.perform(
                        multipart(
                                HttpMethod.PUT,
                                "/api/usuarios/me/foto"
                        )
                                .file(foto)
                                .with(csrf())
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                fotoPerfilService
        );
    }

    @Test
    void deveBloquearConsultaDeFotoSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/usuarios/me/foto")
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                fotoPerfilService
        );
    }

    @Test
    void deveBloquearRemocaoDeFotoSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        delete("/api/usuarios/me/foto")
                                .with(csrf())
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(
                fotoPerfilService
        );
    }

    @Test
    void deveRetornarBadRequestQuandoFotoForInvalida()
            throws Exception {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "arquivo.txt",
                        MediaType.TEXT_PLAIN_VALUE,
                        "arquivo-invalido".getBytes()
                );

        when(
                fotoPerfilService.atualizarFoto(
                        eq(1L),
                        any(MockMultipartFile.class)
                )
        ).thenThrow(
                new FotoPerfilInvalidaException(
                        "A foto deve estar no formato JPEG, PNG ou WebP."
                )
        );

        mockMvc.perform(
                        multipart(
                                HttpMethod.PUT,
                                "/api/usuarios/me/foto"
                        )
                                .file(foto)
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Foto de perfil inválida"
                                )
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "A foto deve estar no formato JPEG, PNG ou WebP."
                                )
                );
    }

    @Test
    void deveRetornarPayloadTooLargeQuandoFotoExcederLimite()
            throws Exception {

        MockMultipartFile foto =
                new MockMultipartFile(
                        "foto",
                        "perfil.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        new byte[]{
                                (byte) 0xFF,
                                (byte) 0xD8,
                                (byte) 0xFF
                        }
                );

        when(
                fotoPerfilService.atualizarFoto(
                        eq(1L),
                        any(MockMultipartFile.class)
                )
        ).thenThrow(
                new FotoPerfilMuitoGrandeException()
        );

        mockMvc.perform(
                        multipart(
                                HttpMethod.PUT,
                                "/api/usuarios/me/foto"
                        )
                                .file(foto)
                                .with(user(usuarioPrincipal()))
                                .with(csrf())
                )
                .andExpect(
                        status().isPayloadTooLarge()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Foto de perfil muito grande"
                                )
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "A foto de perfil deve possuir no máximo 2 MB."
                                )
                );
    }

    @Test
    void deveRetornarNotFoundQuandoUsuarioNaoPossuirFoto()
            throws Exception {

        when(
                fotoPerfilService.buscarFoto(1L)
        ).thenThrow(
                new FotoPerfilNaoEncontradaException()
        );

        mockMvc.perform(
                        get("/api/usuarios/me/foto")
                                .with(user(usuarioPrincipal()))
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.title")
                                .value(
                                        "Foto de perfil não encontrada"
                                )
                )
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Foto de perfil não encontrada."
                                )
                );

        verify(fotoPerfilService)
                .buscarFoto(1L);
    }

    private UsuarioPrincipal usuarioPrincipal() {

        return new UsuarioPrincipal(
                1L,
                "isaque@gamevault.test",
                "{bcrypt}hash"
        );
    }
}
