package com.gamevault.shared.exception;

import com.gamevault.favorito.exception.FavoritoJaExisteException;
import com.gamevault.favorito.exception.FavoritoNaoEncontradoException;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
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
}
