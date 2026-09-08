package com.gamevault.avaliacao.repository;

import com.gamevault.avaliacao.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
}
