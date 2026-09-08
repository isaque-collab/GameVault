package com.gamevault.favorito.repository;

import com.gamevault.favorito.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    boolean existsByUsuarioIdAndRawgGameId(Long userId, Long rawgGameId);

    Optional<Favorito> findByUsuarioIdAndRawgGameId(Long userId, Long rawgGameId);

    List<Favorito> findAllByUsuarioId(Long userId);
}
