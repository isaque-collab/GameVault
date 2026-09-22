package com.gamevault.jogo.service;

import com.gamevault.jogo.dto.ItemColecaoJogoResposta;
import com.gamevault.rawg.client.RawgClient;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import com.gamevault.rawg.exception.JogoRawgNaoEncontradoException;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JogoColecaoService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    JogoColecaoService.class
            );

    private final RawgClient rawgClient;

    public JogoColecaoService(
            RawgClient rawgClient
    ) {
        this.rawgClient = rawgClient;
    }

    public ItemColecaoJogoResposta enriquecer(
            Long id,
            Long rawgGameId,
            LocalDateTime criadoEm
    ) {

        try {

            RawgJogoDetalhesResposta jogo =
                    rawgClient.buscarJogoPorId(
                            rawgGameId
                    );

            return new ItemColecaoJogoResposta(
                    id,
                    rawgGameId,
                    criadoEm,
                    jogo.nome(),
                    jogo.dataLancamento(),
                    jogo.imagemFundo(),
                    jogo.notaRawg(),
                    jogo.metacritic(),
                    true
            );

        } catch (
                JogoRawgNaoEncontradoException
                | RawgApiKeyNaoConfiguradaException
                | RawgIntegracaoException exception
        ) {

            LOGGER.warn(
                    "Não foi possível obter os metadados RAWG do jogo {} para coleção pessoal.",
                    rawgGameId
            );

            return  new ItemColecaoJogoResposta(
                    id,
                    rawgGameId,
                    criadoEm,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false
            );
        }
    }
}
