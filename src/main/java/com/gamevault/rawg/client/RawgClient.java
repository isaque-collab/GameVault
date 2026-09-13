package com.gamevault.rawg.client;

import com.gamevault.rawg.config.RawgProperties;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import com.gamevault.rawg.exception.JogoRawgNaoEncontradoException;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RawgClient {

    private final RestClient restClient;
    private final RawgProperties properties;

    public RawgClient(
            @Qualifier("rawgRestClient") RestClient restClient,
            RawgProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public RawgJogoDetalhesResposta buscarJogoPorId(Long rawgGameId) {
        validarApiKey();

        try {
            RawgJogoDetalhesResposta resposta = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games/{id}")
                            .queryParam("key", properties.apiKey())
                            .build(rawgGameId))
                    .retrieve()
                    .onStatus(
                            status -> status.value()
                            == HttpStatus.NOT_FOUND.value(),
                            (request, response) -> {
                                throw new JogoRawgNaoEncontradoException(
                                        rawgGameId
                                );
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new RawgIntegracaoException(
                                        "A RAWG retornou o status HTTP "
                                        + response.getStatusCode().value()
                                        + "."
                                );
                            }
                    )
                    .body(RawgJogoDetalhesResposta.class);

            if (resposta == null) {
                throw new RawgIntegracaoException(
                        "A RAWG retornou uma resposta sem corpo."
                );
            }

            return resposta;
        } catch (
                JogoRawgNaoEncontradoException
                | RawgIntegracaoException exception
        ) {
            throw exception;
        } catch (RestClientException exception) {
            throw new RawgIntegracaoException(
                    "Não foi possível consultar a RAWG.",
                    exception
            );
        }
    }

    private void validarApiKey() {
        if (properties.apiKey() == null
                || properties.apiKey().isBlank()) {
            throw new RawgApiKeyNaoConfiguradaException();
        }
    }
}
