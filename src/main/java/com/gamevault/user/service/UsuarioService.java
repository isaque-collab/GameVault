package com.gamevault.user.service;

import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.*;
import com.gamevault.user.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(
            String nome,
            String username,
            String email,
            String senha,
            String confirmacaoSenha
    ) {
        validarSenha(
                senha,
                confirmacaoSenha
        );

        validarUnicidade(
                username,
                email
        );

        Usuario usuario = new Usuario();
        usuario.setName(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword(
                passwordEncoder.encode(senha)
        );

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId( Long usuarioId){
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));
    }

    private void validarSenha(
            String senha,
            String confirmacaoSenha
    ) {
        if (senha == null || senha.length() < 8) {
            throw new SenhaInvalidaException();
        }

        if (!senha.equals(confirmacaoSenha)) {
            throw new SenhasNaoCoincidemException();
        }
    }

    private void validarUnicidade(
            String username,
            String email
    ) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new UsernameJaCadastradoException(username);
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
    }

    @Transactional
    public Usuario atualizarPerfil(
            Long usuarioId,
            String nome,
            String username,
            String email,
            String imagemPerfil
    ) {
        Usuario usuario = buscarPorId(usuarioId);

        validarUnicidadeNaAtualizacao(
                usuarioId,
                username,
                email
        );

        usuario.setName(nome);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setProfileImageUrl(imagemPerfil);

        return usuario;

    }

    private void validarUnicidadeNaAtualizacao(
            Long usuarioId,
            String username,
            String email
    ) {
        if (
                usuarioRepository.existsByUsernameAndIdNot(
                        username,
                        usuarioId
                )
        ) {
            throw new UsernameJaCadastradoException(
                    username
            );
        }

        if (
                usuarioRepository.existsByEmailAndIdNot(
                        email,
                        usuarioId
                )
        ) {
            throw new EmailJaCadastradoException(
                    email
            );
        }
    }

    @Transactional
    public void alterarSenha(
            Long usuarioId,
            String senhaAtual,
            String novaSenha,
            String confirmacaoSenha
    ) {
        Usuario usuario =
                buscarPorId(usuarioId);

        if (
                !passwordEncoder.matches(
                        senhaAtual,
                        usuario.getPassword()
                )
        ) {
            throw new SenhaAtualInvalidaException();
        }

        validarSenha(
                novaSenha,
                confirmacaoSenha
        );

        usuario.setPassword(
                passwordEncoder.encode(novaSenha)
        );

    }

    @Transactional
    public void excluirConta(Long usuarioId) {

        Usuario usuario =
                buscarPorId(usuarioId);

        usuarioRepository.delete(usuario);
    }
}
