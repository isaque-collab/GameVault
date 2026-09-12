package com.gamevault.shared.exception;

import com.gamevault.avaliacao.exception.AvaliacaoNaoEncontradaException;
import com.gamevault.avaliacao.exception.NotaAvaliacaoInvalidaException;
import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.user.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
            UsuarioNaoEncontradoException.class
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
}
