package com.gamevault.user.repository;


import com.gamevault.user.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void deveSalvarEBuscarUsuarioPorId(){
        User usuario = new User();
        usuario.setName("Usuario Teste");
        usuario.setUsername("usuario_teste");
        usuario.setEmail("usuario.teste@gamevault.test");
        usuario.setPassword("test-password-hash");

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        assertNotNull(usuarioSalvo.getId());

        User usuarioEncontrado = userRepository.findById(usuarioSalvo.getId()).orElseThrow();

        assertAll(
                () -> assertEquals("Usuario Teste", usuarioEncontrado.getName()),
                () -> assertEquals("usuario_teste", usuarioEncontrado.getUsername()),
                () -> assertEquals("usuario.teste@gamevault.test", usuarioEncontrado.getEmail())
        );
    }
}
