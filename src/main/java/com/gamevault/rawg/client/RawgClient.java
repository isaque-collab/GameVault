package com.gamevault.rawg.client;

import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.rawg.config.RawgProperties;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import com.gamevault.rawg.dto.RawgScreenshotsResposta;
import com.gamevault.rawg.exception.JogoRawgNaoEncontradoException;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

@Component
public class RawgClient {

    private static final int TAMANHO_PAGINA_BUSCA = 20;

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

    public RawgScreenshotsResposta buscarScreenshotsPorJogo(
            Long rawgGameId
    ) {
        validarApiKey();

        try {
            RawgScreenshotsResposta resposta = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games/{id}/screenshots")
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
                    .body(RawgScreenshotsResposta.class);

            if (resposta == null) {
                throw new RawgIntegracaoException(
                        "A RAWG retornou uma resposta sem corpo."
                );
            }

            if (resposta.total() == null
                    || resposta.resultados() == null) {
                throw new RawgIntegracaoException(
                        "A RAWG retornou uma resposta inválida."
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

    public RawgBuscaJogosResposta buscarJogos(
            FiltroCatalogoJogos filtro
    ) {
        validarApiKey();
        validarFiltroCatalogo(filtro);

        return executarBusca(filtro);
    }

    private RawgBuscaJogosResposta executarBusca(
            FiltroCatalogoJogos filtro
    ) {
        try {
            RawgBuscaJogosResposta resposta = restClient
                    .get()
                    .uri(uriBuilder -> {
                        UriBuilder construtorUri = uriBuilder
                                .path("/games")
                                .queryParam("key", properties.apiKey());

                        adicionarParametroTexto(
                                construtorUri,
                                "search",
                                filtro.nome()
                        );

                        construtorUri
                                .queryParam("page", filtro.pagina())
                                .queryParam(
                                        "page_size",
                                        TAMANHO_PAGINA_BUSCA
                                );

                        adicionarParametroTexto(
                                construtorUri,
                                "genres",
                                filtro.genero()
                        );

                        if (filtro.plataforma() != null) {
                            construtorUri.queryParam(
                                    "platforms",
                                    filtro.plataforma()
                            );
                        }

                        adicionarParametroTexto(
                                construtorUri,
                                "developers",
                                filtro.desenvolvedora()
                        );

                        adicionarParametroTexto(
                                construtorUri,
                                "publishers",
                                filtro.publicadora()
                        );

                        if (filtro.lancamentoInicio() != null
                                && filtro.lancamentoFim() != null) {
                            construtorUri.queryParam(
                                    "dates",
                                    filtro.lancamentoInicio()
                                            + ","
                                            + filtro.lancamentoFim()
                            );
                        }

                        if (filtro.ordenacao() != null) {
                            construtorUri.queryParam(
                                    "ordering",
                                    filtro.ordenacao()
                                            .getParametroRawg()
                            );
                        }

                        return construtorUri.build();
                    })
                    .retrieve()
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
                    .body(RawgBuscaJogosResposta.class);
            if (resposta == null) {
                throw new RawgIntegracaoException(
                        "A RAWG retornou uma resposta sem corpo."
                );
            }

            if (resposta.total() == null
                    || resposta.resultados() == null) {
                throw new RawgIntegracaoException(
                        "A RAWG retornou uma resposta inválida."
                );
            }

            return resposta;
        } catch (RawgIntegracaoException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new RawgIntegracaoException(
                    "Não foi possível consultar a RAWG.",
                    exception
            );
        }
    }

    private void adicionarParametroTexto(
            UriBuilder uriBuilder,
            String parametro,
            String valor
    ) {
        if (valor != null && !valor.isBlank()) {
            uriBuilder.queryParam(
                    parametro,
                    valor.trim()
            );
        }
    }

    private void validarFiltroCatalogo(
            FiltroCatalogoJogos filtro
    ) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                    "O filtro do catálogo é obrigatório."
            );
        }

        if (filtro.pagina() < 1) {
            throw new IllegalArgumentException(
                    "A página deve ser maior ou igual a 1."
            );
        }

        if (filtro.plataforma() != null
                && filtro.plataforma() < 1) {
            throw new IllegalArgumentException(
                    "A plataforma deve possuir um identificador válido."
            );
        }

        boolean possuiDataInicial =
                filtro.lancamentoInicio() != null;

        boolean possuiDataFinal =
                filtro.lancamentoFim() != null;

        if (possuiDataInicial != possuiDataFinal) {
            throw new IllegalArgumentException(
                    "As datas inicial e final devem ser informadas juntas."
            );
        }

        if (possuiDataInicial
                && possuiDataFinal
                && filtro.lancamentoInicio()
                .isAfter(filtro.lancamentoFim())) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
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
