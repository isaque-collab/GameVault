package com.gamevault.review.repository;

import com.gamevault.review.entity.Review;
import com.gamevault.user.entity.User;
import com.gamevault.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReviewRepositoryIntegrationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void deveSalvarAvaliacaoAssociadaAoUsuario() {
        User usuario = criarUsuario(
                "Usuario Avaliacao",
                "usuario_avaliacao",
                "usuario.avaliacao@gamevault.test"
        );

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Review avaliacao = new Review();
        avaliacao.setUser(usuarioSalvo);
        avaliacao.setRawgGameId(3498L);
        avaliacao.setRating((byte) 5);

        Review avaliacaoSalva = reviewRepository.saveAndFlush(avaliacao);

        assertNotNull(avaliacaoSalva.getId());

        Review avaliacaoEncontrada = reviewRepository
                .findById(avaliacaoSalva.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        usuarioSalvo.getId(),
                        avaliacaoEncontrada.getUser().getId()
                ),
                () -> assertEquals(
                        3498L,
                        avaliacaoEncontrada.getRawgGameId()
                ),
                () -> assertEquals(
                        (byte) 5,
                        avaliacaoEncontrada.getRating()
                )
        );
    }

    @Test
    void deveImpedirAvaliacaoDuplicadaParaMesmoUsuarioEJogo() {
        User usuario = criarUsuario(
                "Usuario Avaliacao Duplicada",
                "usuario_avaliacao_duplicada",
                "usuario.avaliacao.duplicada@gamevault.test"
        );

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Review primeiraAvaliacao = new Review();
        primeiraAvaliacao.setUser(usuarioSalvo);
        primeiraAvaliacao.setRawgGameId(3498L);
        primeiraAvaliacao.setRating((byte) 4);

        reviewRepository.saveAndFlush(primeiraAvaliacao);

        Review avaliacaoDuplicada = new Review();
        avaliacaoDuplicada.setUser(usuarioSalvo);
        avaliacaoDuplicada.setRawgGameId(3498L);
        avaliacaoDuplicada.setRating((byte) 5);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> reviewRepository.saveAndFlush(avaliacaoDuplicada)
        );
    }

    @Test
    void deveRejeitarNotaAbaixoDoMinimoPermitido() {
        User usuario = criarUsuario(
                "Usuario Nota Baixa",
                "usuario_nota_baixa",
                "usuario.nota.baixa@gamevault.test"
        );

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Review avaliacao = new Review();
        avaliacao.setUser(usuarioSalvo);
        avaliacao.setRawgGameId(4200L);
        avaliacao.setRating((byte) 0);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> reviewRepository.saveAndFlush(avaliacao)
        );
    }

    @Test
    void deveRejeitarNotaAcimaDoMaximoPermitido() {
        User usuario = criarUsuario(
                "Usuario Nota Alta",
                "usuario_nota_alta",
                "usuario.nota.alta@gamevault.test"
        );

        User usuarioSalvo = userRepository.saveAndFlush(usuario);

        Review avaliacao = new Review();
        avaliacao.setUser(usuarioSalvo);
        avaliacao.setRawgGameId(4200L);
        avaliacao.setRating((byte) 6);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> reviewRepository.saveAndFlush(avaliacao)
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