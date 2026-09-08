package com.gamevault.favorito.repository;

import com.gamevault.favorito.entity.Favorito;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FavoritoRepositoryIntegrationTest {

    @Autowired
    private FavoritoRepository favoritoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveSalvarFavoritoAssociadoAoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setName("Usuario Favorito");
        usuario.setUsername("usuario_favorito");
        usuario.setEmail("usuario.favorito@gamevault.test");
        usuario.setPassword("test-password-hash");

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuarioSalvo);
        favorito.setRawgGameId(3498L);

        Favorito favoritoSalvo = favoritoRepository.saveAndFlush(favorito);

        assertNotNull(favoritoSalvo.getId());

        Favorito favoritoEncontrado = favoritoRepository
                .findById(favoritoSalvo.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(usuarioSalvo.getId(), favoritoEncontrado.getUseario().getId()),
                () -> assertEquals(3498L, favoritoEncontrado.getRawgGameId())
        );
    }

    @Test
    void deveImpedirFavoritoDuplicadoParaMesmoUsuarioEJogo() {
        Usuario usuario = new Usuario();
        usuario.setName("Usuario Duplicado");
        usuario.setUsername("usuario_duplicado");
        usuario.setEmail("usuario.duplicado@gamevault.test");
        usuario.setPassword("test-password-hash");

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Favorito primeiroFavorito = new Favorito();
        primeiroFavorito.setUsuario(usuarioSalvo);
        primeiroFavorito.setRawgGameId(3498L);

        favoritoRepository.saveAndFlush(primeiroFavorito);

        Favorito favoritoDuplicado = new Favorito();
        favoritoDuplicado.setUsuario(usuarioSalvo);
        favoritoDuplicado.setRawgGameId(3498L);

        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> favoritoRepository.saveAndFlush(favoritoDuplicado)
        );
    }
}