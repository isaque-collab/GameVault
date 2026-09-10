package com.gamevault.user.security;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void configurar() {
        usuarioDetailsService =
                new UsuarioDetailsService(usuarioRepository);
    }

    @Test
    void deveCarregarUsuarioPorEmail() {
        Usuario usuario = mock(Usuario.class);

        when(usuario.getId())
                .thenReturn(1L);

        when(usuario.getEmail())
                .thenReturn("usuario@gamevault.test");

        when(usuario.getPassword())
                .thenReturn("{bcrypt}hash");

        when(
                usuarioRepository.findByEmail(
                        "usuario@gamevault.test"
                )
        ).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioDetailsService.loadUserByUsername(
                        "usuario@gamevault.test"
                );

        assertInstanceOf(
                UsuarioPrincipal.class,
                resultado
        );

        UsuarioPrincipal principal =
                (UsuarioPrincipal) resultado;

        assertAll(
                () -> assertEquals(
                        1L,
                        principal.getId()
                ),
                () -> assertEquals(
                        "usuario@gamevault.test",
                        principal.getUsername()
                ),
                () -> assertEquals(
                        "{bcrypt}hash",
                        principal.getPassword()
                )
        );

        verify(usuarioRepository)
                .findByEmail(
                        "usuario@gamevault.test"
                );
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoForEncontrado() {
        when(
                usuarioRepository.findByEmail(
                        "inexistente@gamevault.test"
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () ->
                        usuarioDetailsService.loadUserByUsername(
                                "inexistente@gamevault.test"
                        )
        );

        verify(usuarioRepository)
                .findByEmail(
                        "inexistente@gamevault.test"
                );
    }
}
