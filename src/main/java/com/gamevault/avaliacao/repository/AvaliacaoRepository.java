package com.gamevault.avaliacao.repository;

import com.gamevault.avaliacao.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByUsuarioIdAndRawgGameId(
            Long usuarioId,
            Long rawgGameId
    );

    long countByRawgGameId(Long rawgGameId);

    @Query("""
            SELECT AVG(a.rating)
            FROM Avaliacao a
            WHERE a.rawgGameId = :rawgGameId
            """)
    Optional<Double> calcularMediaPorJogo(
            @Param("rawgGameId") Long rawgGameId
    );
}
