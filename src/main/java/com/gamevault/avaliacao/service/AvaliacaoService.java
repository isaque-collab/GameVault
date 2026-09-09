package com.gamevault.avaliacao.service;


import com.gamevault.avaliacao.entity.Avaliacao;
import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.exception.NotaAvaliacaoInvalidaException;
import com.gamevault.avaliacao.repository.AvaliacaoRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Avaliacao avaliar(
            Long usuarioId,
            Long rawgGameId,
            Byte nota
    ) {
        validarNota(nota);

        Optional<Avaliacao> avaliacaoExistente =
                avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                        usuarioId,
                        rawgGameId
                );

        if (avaliacaoExistente.isPresent()){
            Avaliacao avaliacao = avaliacaoExistente.get();
            avaliacao.setRating(nota);

            return avaliacao;
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(
                        () -> new UsuarioNaoEncontradoException(usuarioId)
                );

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setRawgGameId(rawgGameId);
        avaliacao.setRating(nota);

        return avaliacaoRepository.save(avaliacao);
    }

    @Transactional
    public void remover(
            Long usuarioId,
            Long rawgGameId
    ) {
        Avaliacao avaliacao = avaliacaoRepository
                .findByUsuarioIdAndRawgGameId(usuarioId, rawgGameId)
                .orElseThrow(
                        () -> new AvaliacaoNaoEncontradaException(
                                usuarioId,
                                rawgGameId
                        )
                );
        avaliacaoRepository.delete(avaliacao);
    }

    public Optional<Avaliacao> buscarAvaliacaoDoUsuario(
            Long usuarioId,
            Long rawgGameId
    ) {
        return avaliacaoRepository.findByUsuarioIdAndRawgGameId(
                usuarioId,
                rawgGameId);
    }

    public Optional<Double> calcularMediaPorJogo(Long rawgGameId){
        return avaliacaoRepository.calcularMediaPorJogo(rawgGameId);
    }

    public long contarAvaliacoes(Long rawgGameId){
        return avaliacaoRepository.countByRawgGameId(rawgGameId);
    }

    private void validarNota(Byte nota){
        if (nota == null || nota < 1 || nota > 5){
            throw new NotaAvaliacaoInvalidaException();
        }
    }
}
