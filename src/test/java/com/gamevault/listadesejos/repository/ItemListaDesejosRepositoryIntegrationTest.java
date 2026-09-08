package com.gamevault.listadesejos.repository;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import com.gamevault.listadesejos.entity.ItemListaDesejos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemListaDesejosRepositoryIntegrationTest {

    @Autowired
    private ItemListaDesejosRepository itemListaDesejosRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveSalvarJogoNaWishlistAssociadoAoUsuario() {
        Usuario usuario = criarUsuario(
                "Usuario Wishlist",
                "usuario_wishlist",
                "usuario.wishlist@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        ItemListaDesejos itemItemListaDesejos = new ItemListaDesejos();
        itemItemListaDesejos.setUser(usuarioSalvo);
        itemItemListaDesejos.setRawgGameId(4200L);

        ItemListaDesejos itemSalvo = itemListaDesejosRepository.saveAndFlush(itemItemListaDesejos);

        assertNotNull(itemSalvo.getId());

        ItemListaDesejos itemEncontrado = itemListaDesejosRepository
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
        Usuario usuario = criarUsuario(
                "Usuario Wishlist Duplicada",
                "usuario_wishlist_duplicada",
                "usuario.wishlist.duplicada@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        ItemListaDesejos primeiroItem = new ItemListaDesejos();
        primeiroItem.setUser(usuarioSalvo);
        primeiroItem.setRawgGameId(4200L);

        itemListaDesejosRepository.saveAndFlush(primeiroItem);

        ItemListaDesejos itemDuplicado = new ItemListaDesejos();
        itemDuplicado.setUser(usuarioSalvo);
        itemDuplicado.setRawgGameId(4200L);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> itemListaDesejosRepository.saveAndFlush(itemDuplicado)
        );
    }

    private Usuario criarUsuario(
            String nome,
            String username,
            String email
    ) {
        Usuario usuario = new Usuario();
        usuario.setName(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword("test-password-hash");

        return usuario;
    }
}