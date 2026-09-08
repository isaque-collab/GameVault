package com.gamevault.favorito.repository;

import com.gamevault.favorito.entity.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    boolean existsByUserIdAndRawgGameId(Long userId, Long rawgGameId);

    Optional<Favorito> findByUserIdAndRawgGameId(Long userId, Long rawgGameId);

    List<Favorito> findAllByUserId(Long userId);
}
