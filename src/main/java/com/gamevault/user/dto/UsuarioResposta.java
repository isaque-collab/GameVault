package com.gamevault.user.dto;

import com.gamevault.user.entity.Usuario;

public record UsuarioResposta(
        Long id,
        String nome,
        String username,
        String email,
        String imagemPerfil
) {

    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(
                usuario.getId(),
                usuario.getName(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getProfileImageUrl()
        );
    }
}
