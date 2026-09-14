package com.gamevault.rawg.client;

import com.gamevault.rawg.config.RawgConfig;
import com.gamevault.rawg.config.RawgProperties;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import com.gamevault.rawg.dto.RawgJogoResumoResposta;
import com.gamevault.rawg.exception.JogoRawgNaoEncontradoException;
import com.gamevault.rawg.exception.RawgApiKeyNaoConfiguradaException;
import com.gamevault.rawg.exception.RawgIntegracaoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;


import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;

@RestClientTest(RawgClient.class)
@Import(RawgConfig.class)
@TestPropertySource(properties = {
        "rawg.base-url=https://api.rawg.io/api",
        "rawg.api-key=chave-teste"
})
class RawgClientTest {

    private static final String BASE_URL =
            "https://api.rawg.io/api";

    @Autowired
    private RawgClient rawgClient;

    @Autowired
    private MockRestServiceServer servidor;

    @Test
    void deveBuscarJogoPorIdEMapearRespostaDaRawg() {
        String respostaRawg = """
                {
                  "id": 3498,
                  "name": "Grand Theft Auto V",
                  "released": "2013-09-17",
                  "background_image": "https://exemplo.com/gta-v.jpg",
                  "rating": 4.47,
                  "ratings_count": 7000,
                  "metacritic": 92,
                  "campo_ignorado": "valor ignorado"
                }
                """;

        servidor.expect(requestTo(
                        "https://api.rawg.io/api/games/3498?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(respostaRawg, MediaType.APPLICATION_JSON));

        RawgJogoDetalhesResposta resultado =
                rawgClient.buscarJogoPorId(3498L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(3498L);
        assertThat(resultado.nome()).isEqualTo("Grand Theft Auto V");
        assertThat(resultado.dataLancamento())
                .isEqualTo(LocalDate.of(2013, 9, 17));
        assertThat(resultado.imagemFundo())
                .isEqualTo("https://exemplo.com/gta-v.jpg");
        assertThat(resultado.notaRawg()).isEqualTo(4.47);
        assertThat(resultado.totalAvaliacoes()).isEqualTo(7000);
        assertThat(resultado.metacritic()).isEqualTo(92);

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoJogoNaoForEncontradoNaRawg() {
        servidor.expect(requestTo(
                        BASE_URL + "/games/999999?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(
                () -> rawgClient.buscarJogoPorId(999999L)
        )
                .isInstanceOf(
                        JogoRawgNaoEncontradoException.class
                )
                .hasMessage(
                        "Jogo não encontrado na RAWG: 999999"
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoRawgRetornarErro() {
        servidor.expect(requestTo(
                        BASE_URL + "/games/3498?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                );

        assertThatThrownBy(
                () -> rawgClient.buscarJogoPorId(3498L)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "A RAWG retornou o status HTTP 500."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoRawgRetornarRespostaSemCorpo() {
        servidor.expect(requestTo(
                        BASE_URL + "/games/3498?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "",
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(
                () -> rawgClient.buscarJogoPorId(3498L)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "A RAWG retornou uma resposta sem corpo."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoApiKeyNaoEstiverConfigurada() {
        RawgProperties propriedadesSemChave =
                new RawgProperties(BASE_URL, " ");

        RestClient clienteHttp = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();

        RawgClient clienteSemChave = new RawgClient(
                clienteHttp,
                propriedadesSemChave
        );

        assertThatThrownBy(
                () -> clienteSemChave.buscarJogoPorId(3498L)
        )
                .isInstanceOf(
                        RawgApiKeyNaoConfiguradaException.class
                )
                .hasMessage(
                        "A chave da API RAWG não está configurada."
                );
    }

    @Test
    void deveLancarExcecaoQuandoFalharComunicacaoComRawg() {
        servidor.expect(requestTo(
                        BASE_URL + "/games/3498?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(
                        new IOException("Falha de conexão simulada")
                ));

        assertThatThrownBy(
                () -> rawgClient.buscarJogoPorId(3498L)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "Não foi possível consultar a RAWG."
                )
                .hasCauseInstanceOf(RestClientException.class);

        servidor.verify();
    }

    @Test
    void deveBuscarJogosPorNomeComPaginacaoEMapearRespostaDaRawg() {
        String respostaRawg = """
            {
              "count": 41,
              "next": "https://api.rawg.io/api/games?page=3",
              "previous": "https://api.rawg.io/api/games?page=1",
              "results": [
                {
                  "id": 4200,
                  "name": "Portal 2",
                  "released": "2011-04-18",
                  "background_image": "https://exemplo.com/portal-2.jpg",
                  "rating": 4.61,
                  "ratings_count": 6900,
                  "metacritic": 95,
                  "campo_ignorado": "valor"
                },
                {
                  "id": 13536,
                  "name": "Portal",
                  "released": "2007-10-09",
                  "background_image": "https://exemplo.com/portal.jpg",
                  "rating": 4.50,
                  "ratings_count": 5000,
                  "metacritic": 90
                }
              ]
            }
            """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=Portal"
                                + "&page=2"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        RawgBuscaJogosResposta resultado =
                rawgClient.buscarJogosPorNome(" Portal ", 2);

        assertThat(resultado.total()).isEqualTo(41);
        assertThat(resultado.resultados()).hasSize(2);
        assertThat(resultado.resultados())
                .extracting(RawgJogoResumoResposta::nome)
                .containsExactly("Portal 2", "Portal");

        RawgJogoResumoResposta primeiroJogo =
                resultado.resultados().get(0);

        assertThat(primeiroJogo.id()).isEqualTo(4200L);
        assertThat(primeiroJogo.dataLancamento())
                .isEqualTo(LocalDate.of(2011, 4, 18));
        assertThat(primeiroJogo.imagemFundo())
                .isEqualTo("https://exemplo.com/portal-2.jpg");
        assertThat(primeiroJogo.notaRawg()).isEqualTo(4.61);
        assertThat(primeiroJogo.totalAvaliacoes()).isEqualTo(6900);
        assertThat(primeiroJogo.metacritic()).isEqualTo(95);

        servidor.verify();
    }

    @Test
    void deveRetornarListaVaziaQuandoBuscaNaoEncontrarJogos() {
        String respostaRawg = """
            {
              "count": 0,
              "next": null,
              "previous": null,
              "results": []
            }
            """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=JogoInexistente"
                                + "&page=1"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        RawgBuscaJogosResposta resultado =
                rawgClient.buscarJogosPorNome(
                        "JogoInexistente",
                        1
                );

        assertThat(resultado.total()).isZero();
        assertThat(resultado.resultados()).isEmpty();

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoNomeDaBuscaEstiverEmBranco() {
        assertThatThrownBy(
                () -> rawgClient.buscarJogosPorNome("   ", 1)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O nome do jogo é obrigatório.");

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoPaginaDaBuscaForInvalida() {
        assertThatThrownBy(
                () -> rawgClient.buscarJogosPorNome("Portal", 0)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A página deve ser maior ou igual a 1."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoBuscaRawgRetornarErro() {
        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=Portal"
                                + "&page=1"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(
                        HttpStatus.INTERNAL_SERVER_ERROR
                ));

        assertThatThrownBy(
                () -> rawgClient.buscarJogosPorNome("Portal", 1)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "A RAWG retornou o status HTTP 500."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoBuscaRawgRetornarRespostaSemCorpo() {
        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=Portal"
                                + "&page=1"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "",
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(
                () -> rawgClient.buscarJogosPorNome("Portal", 1)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "A RAWG retornou uma resposta sem corpo."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoBuscaRawgRetornarRespostaInvalida() {
        String respostaRawg = """
            {
              "count": 1
            }
            """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=Portal"
                                + "&page=1"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(
                () -> rawgClient.buscarJogosPorNome("Portal", 1)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "A RAWG retornou uma resposta inválida."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoFalharComunicacaoDuranteBusca() {
        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=Portal"
                                + "&page=1"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(
                        new IOException(
                                "Falha de conexão simulada"
                        )
                ));

        assertThatThrownBy(
                () -> rawgClient.buscarJogosPorNome("Portal", 1)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "Não foi possível consultar a RAWG."
                )
                .hasCauseInstanceOf(RestClientException.class);

        servidor.verify();
    }
}
