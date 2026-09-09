package com.gamevault.listadesejos.repository;

import com.gamevault.listadesejos.entity.ItemListaDesejos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemListaDesejosRepository extends JpaRepository<ItemListaDesejos, Long> {

    boolean existsByUsuarioIdAndRawgGameId(
            Long userId,
            Long rawgGameId
    );

    Optional<ItemListaDesejos> findByUsuarioIdAndRawgGameId(
            Long userId,
            Long rawgGameId
    );

    List<ItemListaDesejos> findAllByUsuarioId(Long userId);
}
