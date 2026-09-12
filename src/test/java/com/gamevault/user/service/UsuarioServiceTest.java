package com.gamevault.user.service;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.*;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveCadastrarUsuarioComSenhaCodificada() {

        String senha = "senha123";
        String senhaCodificada = "{bcrypt}hash-teste";

        when(
                usuarioRepository.existsByUsername(
                        "isaque"
                )
        ).thenReturn(false);

        when(
                usuarioRepository.existsByEmail(
                        "isaque@gamevault.test"
                )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(senha)
        ).thenReturn(senhaCodificada);

        when(
                usuarioRepository.save(
                        any(Usuario.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        Usuario resultado =
                usuarioService.cadastrar(
                        "Isaque",
                        "isaque",
                        "isaque@gamevault.test",
                        senha,
                        senha
                );

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(
                        Usuario.class
                );

        verify(usuarioRepository)
                .save(captor.capture());

        Usuario usuarioPersistido =
                captor.getValue();

        assertAll(
                () -> assertEquals(
                        "Isaque",
                        resultado.getName()
                ),
                () -> assertEquals(
                        "isaque",
                        resultado.getUsername()
                ),
                () -> assertEquals(
                        "isaque@gamevault.test",
                        resultado.getEmail()
                ),
                () -> assertEquals(
                        senhaCodificada,
                        usuarioPersistido.getPassword()
                ),
                () -> assertNotEquals(
                        senha,
                        usuarioPersistido.getPassword()
                )
        );

        verify(passwordEncoder)
                .encode(senha);
    }

    @Test
    void deveRejeitarSenhaComMenosDeOitoCaracteres() {

        assertThrows(
                SenhaInvalidaException.class,
                () -> usuarioService.cadastrar(
                        "Isaque",
                        "isaque",
                        "isaque@gamevault.test",
                        "1234567",
                        "1234567"
                )
        );

        verifyNoInteractions(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveRejeitarSenhaNula() {

        assertThrows(
                SenhaInvalidaException.class,
                () -> usuarioService.cadastrar(
                        "Isaque",
                        "isaque",
                        "isaque@gamevault.test",
                        null,
                        null
                )
        );

        verifyNoInteractions(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveRejeitarQuandoSenhasNaoCoincidem() {

        assertThrows(
                SenhasNaoCoincidemException.class,
                () -> usuarioService.cadastrar(
                        "Isaque",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha456"
                )
        );

        verifyNoInteractions(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveRejeitarUsernameJaCadastrado() {

        when(
                usuarioRepository.existsByUsername(
                        "isaque"
                )
        ).thenReturn(true);

        assertThrows(
                UsernameJaCadastradoException.class,
                () -> usuarioService.cadastrar(
                        "Isaque",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha123"
                )
        );

        verify(usuarioRepository)
                .existsByUsername("isaque");

        verify(
                usuarioRepository,
                never()
        ).existsByEmail(anyString());

        verify(
                usuarioRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                passwordEncoder
        );
    }

    @Test
    void deveRejeitarEmailJaCadastrado() {

        when(
                usuarioRepository.existsByUsername(
                        "isaque"
                )
        ).thenReturn(false);

        when(
                usuarioRepository.existsByEmail(
                        "isaque@gamevault.test"
                )
        ).thenReturn(true);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.cadastrar(
                        "Isaque",
                        "isaque",
                        "isaque@gamevault.test",
                        "senha123",
                        "senha123"
                )
        );

        verify(usuarioRepository)
                .existsByEmail(
                        "isaque@gamevault.test"
                );

        verify(
                usuarioRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                passwordEncoder
        );
    }

    @Test
    void deveBuscarUsuarioPorId() {

        Usuario usuario = new Usuario();
        usuario.setName("Isaque");

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        Usuario resultado =
                usuarioService.buscarPorId(1L);

        assertSame(
                usuario,
                resultado
        );

        verify(usuarioRepository)
                .findById(1L);
    }

    @Test
    void deveRetornarErroQuandoUsuarioNaoForEncontradoPorId() {

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> usuarioService.buscarPorId(1L)
        );

        verify(usuarioRepository)
                .findById(1L);
    }

    @Test
    void deveAtualizarPerfilDoUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Nome Antigo");
        usuario.setUsername("username_antigo");
        usuario.setEmail(
                "antigo@gamevault.test"
        );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                usuarioRepository
                        .existsByUsernameAndIdNot(
                                "novo_username",
                                1L
                        )
        ).thenReturn(false);

        when(
                usuarioRepository
                        .existsByEmailAndIdNot(
                                "novo@gamevault.test",
                                1L
                        )
        ).thenReturn(false);

        Usuario resultado =
                usuarioService.atualizarPerfil(
                        1L,
                        "Novo Nome",
                        "novo_username",
                        "novo@gamevault.test",
                        "https://exemplo.com/foto.jpg"
                );

        assertAll(
                () -> assertSame(
                        usuario,
                        resultado
                ),
                () -> assertEquals(
                        "Novo Nome",
                        resultado.getName()
                ),
                () -> assertEquals(
                        "novo_username",
                        resultado.getUsername()
                ),
                () -> assertEquals(
                        "novo@gamevault.test",
                        resultado.getEmail()
                ),
                () -> assertEquals(
                        "https://exemplo.com/foto.jpg",
                        resultado.getProfileImageUrl()
                )
        );

        verify(usuarioRepository)
                .findById(1L);

        verify(usuarioRepository)
                .existsByUsernameAndIdNot(
                        "novo_username",
                        1L
                );

        verify(usuarioRepository)
                .existsByEmailAndIdNot(
                        "novo@gamevault.test",
                        1L
                );

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void devePermitirManterUsernameEEmailDoProprioUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Isaque");
        usuario.setUsername("isaque");
        usuario.setEmail(
                "isaque@gamevault.test"
        );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                usuarioRepository
                        .existsByUsernameAndIdNot(
                                "isaque",
                                1L
                        )
        ).thenReturn(false);

        when(
                usuarioRepository
                        .existsByEmailAndIdNot(
                                "isaque@gamevault.test",
                                1L
                        )
        ).thenReturn(false);

        Usuario resultado =
                usuarioService.atualizarPerfil(
                        1L,
                        "Isaque Costa",
                        "isaque",
                        "isaque@gamevault.test",
                        null
                );

        assertAll(
                () -> assertEquals(
                        "Isaque Costa",
                        resultado.getName()
                ),
                () -> assertEquals(
                        "isaque",
                        resultado.getUsername()
                ),
                () -> assertEquals(
                        "isaque@gamevault.test",
                        resultado.getEmail()
                ),
                () -> assertNull(
                        resultado.getProfileImageUrl()
                )
        );

        verify(usuarioRepository)
                .existsByUsernameAndIdNot(
                        "isaque",
                        1L
                );

        verify(usuarioRepository)
                .existsByEmailAndIdNot(
                        "isaque@gamevault.test",
                        1L
                );

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarUsernamePertencenteAOutroUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Isaque");
        usuario.setUsername("isaque");
        usuario.setEmail(
                "isaque@gamevault.test"
        );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                usuarioRepository
                        .existsByUsernameAndIdNot(
                                "username_existente",
                                1L
                        )
        ).thenReturn(true);

        assertThrows(
                UsernameJaCadastradoException.class,
                () -> usuarioService.atualizarPerfil(
                        1L,
                        "Isaque",
                        "username_existente",
                        "isaque@gamevault.test",
                        null
                )
        );

        verify(usuarioRepository)
                .existsByUsernameAndIdNot(
                        "username_existente",
                        1L
                );

        verify(
                usuarioRepository,
                never()
        ).existsByEmailAndIdNot(
                anyString(),
                anyLong()
        );

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarEmailPertencenteAOutroUsuario() {

        Usuario usuario = new Usuario();
        usuario.setName("Isaque");
        usuario.setUsername("isaque");
        usuario.setEmail(
                "isaque@gamevault.test"
        );

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                usuarioRepository
                        .existsByUsernameAndIdNot(
                                "isaque",
                                1L
                        )
        ).thenReturn(false);

        when(
                usuarioRepository
                        .existsByEmailAndIdNot(
                                "email.existente@gamevault.test",
                                1L
                        )
        ).thenReturn(true);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> usuarioService.atualizarPerfil(
                        1L,
                        "Isaque",
                        "isaque",
                        "email.existente@gamevault.test",
                        null
                )
        );

        verify(usuarioRepository)
                .existsByUsernameAndIdNot(
                        "isaque",
                        1L
                );

        verify(usuarioRepository)
                .existsByEmailAndIdNot(
                        "email.existente@gamevault.test",
                        1L
                );

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRetornarErroAoAtualizarPerfilDeUsuarioInexistente() {

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> usuarioService.atualizarPerfil(
                        1L,
                        "Novo Nome",
                        "novo_username",
                        "novo@gamevault.test",
                        null
                )
        );

        verify(usuarioRepository)
                .findById(1L);

        verify(
                usuarioRepository,
                never()
        ).existsByUsernameAndIdNot(
                anyString(),
                anyLong()
        );

        verify(
                usuarioRepository,
                never()
        ).existsByEmailAndIdNot(
                anyString(),
                anyLong()
        );

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void deveAlterarSenhaDoUsuario() {

        Usuario usuario = new Usuario();
        usuario.setPassword("{bcrypt}hash-antigo");

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                passwordEncoder.matches(
                        "senhaAtual123",
                        "{bcrypt}hash-antigo"
                )
        ).thenReturn(true);

        when(
                passwordEncoder.encode(
                        "novaSenha123"
                )
        ).thenReturn(
                "{bcrypt}hash-novo"
        );

        usuarioService.alterarSenha(
                1L,
                "senhaAtual123",
                "novaSenha123",
                "novaSenha123"
        );

        assertEquals(
                "{bcrypt}hash-novo",
                usuario.getPassword()
        );

        verify(passwordEncoder)
                .matches(
                        "senhaAtual123",
                        "{bcrypt}hash-antigo"
                );

        verify(passwordEncoder)
                .encode(
                        "novaSenha123"
                );

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarAlteracaoQuandoSenhaAtualEstiverIncorreta() {

        Usuario usuario = new Usuario();
        usuario.setPassword("{bcrypt}hash-antigo");

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                passwordEncoder.matches(
                        "senhaErrada",
                        "{bcrypt}hash-antigo"
                )
        ).thenReturn(false);

        assertThrows(
                SenhaAtualInvalidaException.class,
                () -> usuarioService.alterarSenha(
                        1L,
                        "senhaErrada",
                        "novaSenha123",
                        "novaSenha123"
                )
        );

        assertEquals(
                "{bcrypt}hash-antigo",
                usuario.getPassword()
        );

        verify(
                passwordEncoder,
                never()
        ).encode(anyString());

        verify(
                usuarioRepository,
                never()
        ).save(any());
    }

    @Test
    void deveRejeitarNovaSenhaComMenosDeOitoCaracteres() {

        Usuario usuario = new Usuario();
        usuario.setPassword("{bcrypt}hash-antigo");

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                passwordEncoder.matches(
                        "senhaAtual123",
                        "{bcrypt}hash-antigo"
                )
        ).thenReturn(true);

        assertThrows(
                SenhaInvalidaException.class,
                () -> usuarioService.alterarSenha(
                        1L,
                        "senhaAtual123",
                        "1234567",
                        "1234567"
                )
        );

        verify(
                passwordEncoder,
                never()
        ).encode(anyString());
    }

    @Test
    void deveRejeitarAlteracaoQuandoNovaSenhaEConfirmacaoNaoCoincidirem() {

        Usuario usuario = new Usuario();
        usuario.setPassword("{bcrypt}hash-antigo");

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        when(
                passwordEncoder.matches(
                        "senhaAtual123",
                        "{bcrypt}hash-antigo"
                )
        ).thenReturn(true);

        assertThrows(
                SenhasNaoCoincidemException.class,
                () -> usuarioService.alterarSenha(
                        1L,
                        "senhaAtual123",
                        "novaSenha123",
                        "outraSenha123"
                )
        );

        verify(
                passwordEncoder,
                never()
        ).encode(anyString());
    }

    @Test
    void deveRetornarErroAoAlterarSenhaDeUsuarioInexistente() {

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> usuarioService.alterarSenha(
                        1L,
                        "senhaAtual123",
                        "novaSenha123",
                        "novaSenha123"
                )
        );

        verifyNoInteractions(
                passwordEncoder
        );
    }

    @Test
    void deveExcluirContaDoUsuario() {

        Usuario usuario = new Usuario();

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.of(usuario)
        );

        usuarioService.excluirConta(1L);

        verify(usuarioRepository)
                .findById(1L);

        verify(usuarioRepository)
                .delete(usuario);
    }

    @Test
    void deveRetornarErroAoExcluirContaDeUsuarioInexistente() {

        when(
                usuarioRepository.findById(1L)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> usuarioService.excluirConta(1L)
        );

        verify(
                usuarioRepository,
                never()
        ).delete(any());
    }
}
