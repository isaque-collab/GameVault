package com.gamevault.user.repository;

import com.gamevault.user.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsernameAndIdNot(
            String username,
            Long id
    );

    boolean existsByEmailAndIdNot(
            String email,
            Long id
    );
}
