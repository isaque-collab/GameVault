package com.gamevault.wishlist.repository;

import com.gamevault.user.entity.User;
import com.gamevault.user.repository.UserRepository;
import com.gamevault.wishlist.entity.Wishlist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WishlistRepositoryIntegrationTest {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void deveSalvarJogoNaWishlistAssociadoAoUsuario() {
        User usuario = criarUsuario(
                "Usuario Wishlist",
                "usuario_wishlist",
                "usuario.wishlist@gamevault.test"
        );

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Wishlist itemWishlist = new Wishlist();
        itemWishlist.setUser(usuarioSalvo);
        itemWishlist.setRawgGameId(4200L);

        Wishlist itemSalvo = wishlistRepository.saveAndFlush(itemWishlist);

        assertNotNull(itemSalvo.getId());

        Wishlist itemEncontrado = wishlistRepository
                .findById(itemSalvo.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        usuarioSalvo.getId(),
                        itemEncontrado.getUser().getId()
                ),
                () -> assertEquals(
                        4200L,
                        itemEncontrado.getRawgGameId()
                )
        );
    }

    @Test
    void deveImpedirWishlistDuplicadaParaMesmoUsuarioEJogo() {
        User usuario = criarUsuario(
                "Usuario Wishlist Duplicada",
                "usuario_wishlist_duplicada",
                "usuario.wishlist.duplicada@gamevault.test"
        );

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Wishlist primeiroItem = new Wishlist();
        primeiroItem.setUser(usuarioSalvo);
        primeiroItem.setRawgGameId(4200L);

        wishlistRepository.saveAndFlush(primeiroItem);

        Wishlist itemDuplicado = new Wishlist();
        itemDuplicado.setUser(usuarioSalvo);
        itemDuplicado.setRawgGameId(4200L);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> wishlistRepository.saveAndFlush(itemDuplicado)
        );
    }

    private User criarUsuario(
            String nome,
            String username,
            String email
    ) {
        User usuario = new User();
        usuario.setName(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword("test-password-hash");

        return usuario;
    }
}