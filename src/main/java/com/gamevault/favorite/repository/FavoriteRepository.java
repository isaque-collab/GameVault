package com.gamevault.favorite.repository;

import com.gamevault.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndRawgGameId(Long userId, Long rawgGameId);

    Optional<Favorite> findByUserIdAndRawgGameId(Long userId, Long rawgGameId);

    List<Favorite> findAllByUserId(Long userId);
}
