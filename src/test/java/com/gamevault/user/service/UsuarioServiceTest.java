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

        when(usuarioRepository.existsByUsername("isaque"))
                .thenReturn(false);

        when(usuarioRepository.existsByEmail("isaque@gamevault.test"))
                .thenReturn(false);

        when(passwordEncoder.encode(senha))
                .thenReturn(senhaCodificada);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.cadastrar(
                "Isaque",
                "isaque",
                "isaque@gamevault.test",
                senha,
                senha
        );

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioPersistido = captor.getValue();

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

        verify(passwordEncoder).encode(senha);
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
        when(usuarioRepository.existsByUsername("isaque"))
                .thenReturn(true);

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

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRejeitarEmailJaCadastrado() {
        when(usuarioRepository.existsByUsername("isaque"))
                .thenReturn(false);

        when(usuarioRepository.existsByEmail("isaque@gamevault.test"))
                .thenReturn(true);

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
                .existsByEmail("isaque@gamevault.test");

        verify(
                usuarioRepository,
                never()
        ).save(any());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveBuscarUsuarioPorId() {

        Usuario usuario = new Usuario();
        usuario.setName("Isaque");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

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

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> usuarioService.buscarPorId(1L)
        );

        verify(usuarioRepository)
                .findById(1L);
    }
}
