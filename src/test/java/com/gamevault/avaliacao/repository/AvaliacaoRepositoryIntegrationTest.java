package com.gamevault.avaliacao.repository;

import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AvaliacaoRepositoryIntegrationTest {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveSalvarAvaliacaoAssociadaAoUsuario() {
        Usuario usuario = criarUsuario(
                "Usuario Avaliacao",
                "usuario_avaliacao",
                "usuario.avaliacao@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuarioSalvo);
        avaliacao.setRawgGameId(3498L);
        avaliacao.setRating((byte) 5);

        Avaliacao avaliacaoSalva = avaliacaoRepository.saveAndFlush(avaliacao);

        assertNotNull(avaliacaoSalva.getId());

        Avaliacao avaliacaoEncontrada = avaliacaoRepository
                .findById(avaliacaoSalva.getId())
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        usuarioSalvo.getId(),
                        avaliacaoEncontrada.getUsuario().getId()
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
        Usuario usuario = criarUsuario(
                "Usuario Avaliacao Duplicada",
                "usuario_avaliacao_duplicada",
                "usuario.avaliacao.duplicada@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Avaliacao primeiraAvaliacao = new Avaliacao();
        primeiraAvaliacao.setUsuario(usuarioSalvo);
        primeiraAvaliacao.setRawgGameId(3498L);
        primeiraAvaliacao.setRating((byte) 4);

        avaliacaoRepository.saveAndFlush(primeiraAvaliacao);

        Avaliacao avaliacaoDuplicada = new Avaliacao();
        avaliacaoDuplicada.setUsuario(usuarioSalvo);
        avaliacaoDuplicada.setRawgGameId(3498L);
        avaliacaoDuplicada.setRating((byte) 5);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> avaliacaoRepository.saveAndFlush(avaliacaoDuplicada)
        );
    }

    @Test
    void deveRejeitarNotaAbaixoDoMinimoPermitido() {
        Usuario usuario = criarUsuario(
                "Usuario Nota Baixa",
                "usuario_nota_baixa",
                "usuario.nota.baixa@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuarioSalvo);
        avaliacao.setRawgGameId(4200L);
        avaliacao.setRating((byte) 0);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> avaliacaoRepository.saveAndFlush(avaliacao)
        );
    }

    @Test
    void deveRejeitarNotaAcimaDoMaximoPermitido() {
        Usuario usuario = criarUsuario(
                "Usuario Nota Alta",
                "usuario_nota_alta",
                "usuario.nota.alta@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuarioSalvo);
        avaliacao.setRawgGameId(4200L);
        avaliacao.setRating((byte) 6);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> avaliacaoRepository.saveAndFlush(avaliacao)
        );
    }

    @Test
    void deveBuscarAvaliacaoPorUsuarioEJogo(){
        Usuario usuario = criarUsuario(
                "Usuario Busca Avaliacao",
                "usuario_busca_avaliacao",
                "usuario.busca.avaliacao@gamevault.test"
        );

        Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuarioSalvo);
        avaliacao.setRawgGameId(3498L);
        avaliacao.setRating((byte) 4);

        avaliacaoRepository.saveAndFlush(avaliacao);

        Avaliacao avaliacaoEncontrada = avaliacaoRepository
                .findByUsuarioIdAndRawgGameId(
                        usuarioSalvo.getId(),
                        3498L
                )
                .orElseThrow();

        assertAll(
                () -> assertEquals(
                        usuarioSalvo.getId(),
                        avaliacaoEncontrada.getUsuario().getId()
                ),
                () -> assertEquals(
                        3498L,
                        avaliacaoEncontrada.getRawgGameId()
                ),
                () -> assertEquals(
                        (byte) 4,
                        avaliacaoEncontrada.getRating()
                )
        );
    }

    @Test
    void deveContarAvaliacoesDoJogo() {
        Usuario primeiroUsuario = criarUsuario(
                "Primeiro Usuario Media",
                "primeiro_usuario_media",
                "primeiro.usuario.media@gamevault.test"
        );

        Usuario segundoUsuario = criarUsuario(
                "Segundo Usuario Media",
                "segundo_usuario_media",
                "segundo.usuario.media@gamevault.test"
        );

        usuarioRepository.saveAndFlush(primeiroUsuario);
        usuarioRepository.saveAndFlush(segundoUsuario);

        Avaliacao primeiraAvaliacao = new Avaliacao();
        primeiraAvaliacao.setUsuario(primeiroUsuario);
        primeiraAvaliacao.setRawgGameId(3498L);
        primeiraAvaliacao.setRating((byte) 4);

        Avaliacao segundaAvaliacao = new Avaliacao();
        segundaAvaliacao.setUsuario(segundoUsuario);
        segundaAvaliacao.setRawgGameId(3498L);
        segundaAvaliacao.setRating((byte) 5);

        avaliacaoRepository.saveAndFlush(primeiraAvaliacao);
        avaliacaoRepository.saveAndFlush(segundaAvaliacao);

        long quantidade = avaliacaoRepository.countByRawgGameId(3498L);

        assertEquals(2L, quantidade);
    }

    @Test
    void deveCalcularMediaDasAvaliacoesDoJogo() {
        Usuario primeiroUsuario = criarUsuario(
                "Primeiro Usuario Calculo",
                "primeiro_usuario_calculo",
                "primeiro.usuario.calculo@gamevault.test"
        );

        Usuario segundoUsuario = criarUsuario(
                "Segundo Usuario Calculo",
                "segundo_usuario_calculo",
                "segundo.usuario.calculo@gamevault.test"
        );

        usuarioRepository.saveAndFlush(primeiroUsuario);
        usuarioRepository.saveAndFlush(segundoUsuario);

        Avaliacao primeiraAvaliacao = new Avaliacao();
        primeiraAvaliacao.setUsuario(primeiroUsuario);
        primeiraAvaliacao.setRawgGameId(4200L);
        primeiraAvaliacao.setRating((byte) 4);

        Avaliacao segundaAvaliacao = new Avaliacao();
        segundaAvaliacao.setUsuario(segundoUsuario);
        segundaAvaliacao.setRawgGameId(4200L);
        segundaAvaliacao.setRating((byte) 2);

        avaliacaoRepository.saveAndFlush(primeiraAvaliacao);
        avaliacaoRepository.saveAndFlush(segundaAvaliacao);

        Double media = avaliacaoRepository
                .calcularMediaPorJogo(4200L)
                .orElseThrow();

        assertEquals(3.0, media);
    }

    @Test
    void deveRetornarMediaVaziaQuandoJogoNaoPossuirAvaliacoes() {
        var media = avaliacaoRepository.calcularMediaPorJogo(999999L);

        assertTrue(media.isEmpty());
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