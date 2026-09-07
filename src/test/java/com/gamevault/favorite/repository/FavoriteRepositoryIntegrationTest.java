package com.gamevault.favorite.repository;

import com.gamevault.favorite.entity.Favorite;
import com.gamevault.user.entity.User;
import com.gamevault.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FavoriteRepositoryIntegrationTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void deveSalvarFavoritoAssociadoAoUsuario() {
        User usuario = new User();
        usuario.setName("Usuario Favorito");
        usuario.setUsername("usuario_favorito");
        usuario.setEmail("usuario.favorito@gamevault.test");
        usuario.setPassword("test-password-hash");

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Favorite favorito = new Favorite();
        favorito.setUser(usuarioSalvo);
        favorito.setRawgGameId(3498L);

        Favorite favoritoSalvo = favoriteRepository.saveAndFlush(favorito);

        assertNotNull(favoritoSalvo.getId());

        Favorite favoritoEncontrado = favoriteRepository
                .findById(favoritoSalvo.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(usuarioSalvo.getId(), favoritoEncontrado.getUser().getId()),
                () -> assertEquals(3498L, favoritoEncontrado.getRawgGameId())
        );
    }
}