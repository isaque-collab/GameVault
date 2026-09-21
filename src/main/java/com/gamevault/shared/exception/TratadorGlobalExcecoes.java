package com.gamevault.shared.exception;

import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.exception.NotaAvaliacaoInvalidaException;
import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.rawg.exception.JogoRawgNaoEncontradoException;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import com.gamevault.user.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class TratadorGlobalExcecoes {

    @ExceptionHandler(FavoritoJaExisteException.class)
    public ProblemDetail tratarFavoritoJaExiste(
            FavoritoJaExisteException exception
    ){
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problema.setTitle("Favorito já existente");

        return problema;
    }

    @ExceptionHandler({
            FavoritoNaoEncontradoException.class,
            ItemListaDesejosNaoEncontradoException.class,
            AvaliacaoNaoEncontradaException.class,
            UsuarioNaoEncontradoException.class,
            JogoRawgNaoEncontradoException.class
    })
    public ProblemDetail tratarRecursoNaoEncontrado(
            RuntimeException exception
    ){

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );

        problema.setTitle("Recurso não encontrado");

        return problema;
    }

    @ExceptionHandler(ItemListaDesejosJaExisteException.class)
    public ProblemDetail tratarItemListaDesejosJaExiste(
            ItemListaDesejosJaExisteException exception
    ){

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problema.setTitle("Item já existente na lista de desejos");

        return problema;
    }

    @ExceptionHandler(NotaAvaliacaoInvalidaException.class)
    public ProblemDetail tratarNotaAvaliacaoInvalida(
            NotaAvaliacaoInvalidaException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problema.setTitle("Nota de avaliação inválida");

        return problema;
    }

    @ExceptionHandler({
            UsernameJaCadastradoException.class,
            EmailJaCadastradoException.class
    })
    public ProblemDetail tratarDadosUsuarioJaCadastrado(
            RuntimeException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );

        problema.setTitle("Dados de usuário já cadastrados");

        return problema;
    }

    @ExceptionHandler({
            SenhaInvalidaException.class,
            SenhasNaoCoincidemException.class,
            SenhaAtualInvalidaException.class
    })
    public ProblemDetail tratarSenhaInvalida(
            RuntimeException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problema.setTitle("Senha inválida");

        return problema;
    }

    @ExceptionHandler(RawgIntegracaoException.class)
    public ProblemDetail tratarFalhaIntegracaoRawg(
            RawgIntegracaoException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                exception.getMessage()
        );

        problema.setTitle("Falha na integração com a RAWG");

        return problema;
    }

    @ExceptionHandler(RawgApiKeyNaoConfiguradaException.class)
    public ProblemDetail tratarApiKeyRawgNaoConfigurada(
            RawgApiKeyNaoConfiguradaException exception
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage()
        );

        problema.setTitle("Integração RAWG indisponível");

        return problema;
    }

    @ExceptionHandler(FotoPerfilInvalidaException.class)
    public ProblemDetail tratarFotoPerfilInvalida(
            FotoPerfilInvalidaException exception
    ) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                );

        problema.setTitle(
                "Foto de perfil inválida"
        );

        return problema;
    }

    @ExceptionHandler({
            FotoPerfilMuitoGrandeException.class,
            MaxUploadSizeExceededException.class
    })
    public ProblemDetail tratarFotoPerfilMuitoGrande(
            Exception exception
    ) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "A foto de perfil deve possuir no máximo 2 MB."
                );

        problema.setTitle(
                "Foto de perfil muito grande"
        );

        return problema;
    }

    @ExceptionHandler(FotoPerfilNaoEncontradaException.class)
    public ProblemDetail tratarFotoPerfilNaoEncontrada(
            FotoPerfilNaoEncontradaException exception
    ) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                );

        problema.setTitle(
                "Foto de perfil não encontrada"
        );

        return problema;
    }

    @ExceptionHandler(FotoPerfilArmazenamentoException.class)
    public ProblemDetail tratarFalhaArmazenamentoFoto(
            FotoPerfilArmazenamentoException exception
    ) {

        ProblemDetail problema =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi possível processar a foto de perfil."
                );

        problema.setTitle(
                "Falha no armazenamento da foto"
        );

        return problema;
    }
}
