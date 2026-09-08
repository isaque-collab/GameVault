package com.gamevault.user.repository;


import com.gamevault.user.entity.Usuario;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UsuarioRepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveSalvarEBuscarUsuarioPorId(){
        Usuario usuario = new Usuario();
        usuario.setName("Usuario Teste");
        usuario.setUsername("usuario_teste");
        usuario.setEmail("usuario.teste@gamevault.test");
        usuario.setPassword("test-password-hash");

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        assertNotNull(usuarioSalvo.getId());

        Usuario usuarioEncontrado = usuarioRepository.findById(usuarioSalvo.getId()).orElseThrow();

        assertAll(
                () -> assertEquals("Usuario Teste", usuarioEncontrado.getName()),
                () -> assertEquals("usuario_teste", usuarioEncontrado.getUsername()),
                () -> assertEquals("usuario.teste@gamevault.test", usuarioEncontrado.getEmail())
        );
    }
}
