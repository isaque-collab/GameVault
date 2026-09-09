package com.gamevault.listadesejos.service;

import com.gamevault.listadesejos.entity.ItemListaDesejos;
import com.gamevault.listadesejos.exception.ItemListaDesejosJaExisteException;
import com.gamevault.listadesejos.exception.ItemListaDesejosNaoEncontradoException;
import com.gamevault.listadesejos.repository.ItemListaDesejosRepository;
import com.gamevault.user.entity.Usuario;
import com.gamevault.user.exception.UsuarioNaoEncontradoException;
import com.gamevault.user.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
public class ListaDesejosService {

    private final ItemListaDesejosRepository itemListaDesejosRepository;
    private final UsuarioRepository usuarioRepository;

    public ListaDesejosService(
            ItemListaDesejosRepository itemListaDesejosRepository,
            UsuarioRepository usuarioRepository
    ){
        this.itemListaDesejosRepository = itemListaDesejosRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ItemListaDesejos adicionar(
            Long userId,
            Long rawgGameId
    ){
        if (itemListaDesejosRepository
                .existsByUsuarioIdAndRawgGameId(userId, rawgGameId)){
            throw new ItemListaDesejosJaExisteException(
                    userId,
                    rawgGameId
            );
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(userId));

        ItemListaDesejos item = new ItemListaDesejos();

        item.setUsuario(usuario);
        item.setRawgGameId(rawgGameId);

        return itemListaDesejosRepository.save(item);
    }

    @Transactional
    public void remover(
            Long userId,
            Long rawgGameId
    ){

        ItemListaDesejos item = itemListaDesejosRepository
                .findByUsuarioIdAndRawgGameId(userId, rawgGameId)
                .orElseThrow(
                        () -> new ItemListaDesejosNaoEncontradoException(
                                userId,
                                rawgGameId
                        )
                );

        itemListaDesejosRepository.delete(item);
    }

    public List<ItemListaDesejos> listar(Long userId){
        return itemListaDesejosRepository.findAllByUsuarioId(userId);
    }
}
