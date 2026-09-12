package com.gamevault.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlteracaoSenhaRequisicao(

        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(
                min = 8,
                message = "A nova senha deve possuir no mínimo 8 caracteres"
        )
        String novaSenha,

        @NotBlank(message = "A confirmação da nova senha é obrigatória")
        String confirmacaoNovaSenha
) {
}
