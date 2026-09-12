package com.gamevault.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizacaoPerfilRequisicao(

        @NotBlank(message = "O nome é obrigatório")
        @Size(
                max = 100,
                message = "O nome deve possuir no máximo 100 caracteres"
        )
        String nome,

        @NotBlank(message = "O username é obrigatório")
        @Size(
                max = 50,
                message = "O username deve possuir no máximo 50 caracteres"
        )
        String username,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(
                message = "O e-mail deve possuir um formato válido"
        )
        @Size(
                max = 150,
                message = "O e-mail deve possuir no máximo 150 caracteres"
        )
        String email,

        @Size(
                max = 500,
                message = "A URL da imagem deve possuir no máximo 500 caracteres"
        )
        String imagemPerfil
) {
}
