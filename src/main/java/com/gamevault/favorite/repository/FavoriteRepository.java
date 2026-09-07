package com.gamevault.favorite.repository;

import com.gamevault.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
