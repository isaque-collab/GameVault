package com.gamevault.user.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioPrincipalTest {

    @Test
    void deveRepresentarUsuarioAutenticado() {
        UsuarioPrincipal principal =
                new UsuarioPrincipal(
                        1L,
                        "usuario@gamevault.test",
                        "{bcrypt}hash"
                );

        Collection<? extends GrantedAuthority> authorities =
                principal.getAuthorities();

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
                ),
                () -> assertEquals(
                        1,
                        authorities.size()
                ),
                () -> assertEquals(
                        "ROLE_USER",
                        authorities.iterator()
                                .next()
                                .getAuthority()
                )
        );
    }

    @Test
    void deveApagarCredenciais() {
        UsuarioPrincipal principal =
                new UsuarioPrincipal(
                        1L,
                        "usuario@gamevault.test",
                        "{bcrypt}hash"
                );

        principal.eraseCredentials();

        assertNull(
                principal.getPassword()
        );
    }
}