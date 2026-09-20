package com.gamevault.rawg.client;

import com.gamevault.jogo.dto.FiltroCatalogoJogos;
import com.gamevault.jogo.dto.OrdenacaoJogo;
import com.gamevault.rawg.config.RawgConfig;
import com.gamevault.rawg.config.RawgProperties;
import com.gamevault.rawg.dto.RawgBuscaJogosResposta;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import com.gamevault.rawg.dto.RawgJogoResumoResposta;
import com.gamevault.rawg.dto.RawgScreenshotsResposta;
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

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        " Portal ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        2
                );

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
                rawgClient.buscarJogos(filtro);

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

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        "JogoInexistente",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

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
                rawgClient.buscarJogos(filtro);

        assertThat(resultado.total()).isZero();
        assertThat(resultado.resultados()).isEmpty();

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoBuscaRawgRetornarErro() {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        "Portal",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

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
                () -> rawgClient.buscarJogos(filtro)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "A RAWG retornou o status HTTP 500."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoBuscaRawgRetornarRespostaSemCorpo() {

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        "Portal",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

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
                () -> rawgClient.buscarJogos(filtro)
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

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        "Portal",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

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
                () -> rawgClient.buscarJogos(filtro)
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

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        "Portal",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

        assertThatThrownBy(
                () -> rawgClient.buscarJogos(filtro)
        )
                .isInstanceOf(RawgIntegracaoException.class)
                .hasMessage(
                        "Não foi possível consultar a RAWG."
                )
                .hasCauseInstanceOf(RestClientException.class);

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoSemNome() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&page=1"
                                + "&page_size=20"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

        RawgBuscaJogosResposta resultado =
                rawgClient.buscarJogos(filtro);

        assertThat(resultado.total()).isZero();
        assertThat(resultado.resultados()).isEmpty();

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoFiltrandoPorPlataforma() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&page=1"
                                + "&page_size=20"
                                + "&platforms=4"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        4,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

        rawgClient.buscarJogos(filtro);

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoFiltrandoPorDesenvolvedora() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&page=1"
                                + "&page_size=20"
                                + "&developers=valve"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        " valve ",
                        null,
                        null,
                        null,
                        null,
                        1
                );

        rawgClient.buscarJogos(filtro);

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoFiltrandoPorPublicadora() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&page=1"
                                + "&page_size=20"
                                + "&publishers=electronic-arts"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        " electronic-arts ",
                        null,
                        null,
                        null,
                        1
                );

        rawgClient.buscarJogos(filtro);

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoFiltrandoPorPeriodoDeLancamento() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&page=1"
                                + "&page_size=20"
                                + "&dates=2026-01-01,2026-12-31"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        null,
                        1
                );

        rawgClient.buscarJogos(filtro);

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoOrdenandoPorPopularidade() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&page=1"
                                + "&page_size=20"
                                + "&ordering=-added"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        OrdenacaoJogo.POPULARIDADE,
                        1
                );

        rawgClient.buscarJogos(filtro);

        servidor.verify();
    }

    @Test
    void deveBuscarCatalogoComMultiplosFiltros() {
        String respostaRawg = """
        {
          "count": 0,
          "results": []
        }
        """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games?key=chave-teste"
                                + "&search=Portal"
                                + "&page=2"
                                + "&page_size=20"
                                + "&genres=action"
                                + "&platforms=4"
                                + "&developers=valve"
                                + "&publishers=electronic-arts"
                                + "&dates=2020-01-01,2026-12-31"
                                + "&ordering=-metacritic"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        " Portal ",
                        " action ",
                        4,
                        " valve ",
                        " electronic-arts ",
                        LocalDate.of(2020, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        OrdenacaoJogo.METACRITIC,
                        2
                );

        rawgClient.buscarJogos(filtro);

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoPaginaDoCatalogoForInvalida() {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        0
                );

        assertThatThrownBy(
                () -> rawgClient.buscarJogos(filtro)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A página deve ser maior ou igual a 1."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoPlataformaForInvalida() {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        0,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1
                );

        assertThatThrownBy(
                () -> rawgClient.buscarJogos(filtro)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A plataforma deve possuir um identificador válido."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoSomenteUmaDataForInformada() {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 1, 1),
                        null,
                        null,
                        1
                );

        assertThatThrownBy(
                () -> rawgClient.buscarJogos(filtro)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "As datas inicial e final devem ser informadas juntas."
                );

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoPeriodoDeLancamentoForInvertido() {
        FiltroCatalogoJogos filtro =
                new FiltroCatalogoJogos(
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDate.of(2026, 12, 31),
                        LocalDate.of(2026, 1, 1),
                        null,
                        1
                );

        assertThatThrownBy(
                () -> rawgClient.buscarJogos(filtro)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A data inicial não pode ser posterior à data final."
                );

        servidor.verify();
    }

    @Test
    void deveBuscarScreenshotsDoJogoEMapearRespostaDaRawg() {
        String respostaRawg = """
            {
              "count": 2,
              "next": null,
              "previous": null,
              "results": [
                {
                  "id": 1,
                  "image": "https://exemplo.com/screenshot-1.jpg",
                  "width": 1920,
                  "height": 1080,
                  "hidden": false
                },
                {
                  "id": 2,
                  "image": "https://exemplo.com/screenshot-2.jpg",
                  "width": 1920,
                  "height": 1080,
                  "hidden": true
                }
              ]
            }
            """;

        servidor.expect(requestTo(
                        BASE_URL
                                + "/games/3498/screenshots"
                                + "?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        respostaRawg,
                        MediaType.APPLICATION_JSON
                ));

        RawgScreenshotsResposta resultado =
                rawgClient.buscarScreenshotsPorJogo(3498L);

        assertThat(resultado.total()).isEqualTo(2);
        assertThat(resultado.resultados()).hasSize(2);

        assertThat(resultado.resultados().get(0).image())
                .isEqualTo(
                        "https://exemplo.com/screenshot-1.jpg"
                );

        assertThat(resultado.resultados().get(0).oculto())
                .isFalse();

        assertThat(resultado.resultados().get(1).image())
                .isEqualTo(
                        "https://exemplo.com/screenshot-2.jpg"
                );

        assertThat(resultado.resultados().get(1).oculto())
                .isTrue();

        servidor.verify();
    }

    @Test
    void deveLancarExcecaoQuandoJogoDosScreenshotsNaoForEncontrado() {
        servidor.expect(requestTo(
                        BASE_URL
                                + "/games/999999/screenshots"
                                + "?key=chave-teste"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(
                () -> rawgClient.buscarScreenshotsPorJogo(
                        999999L
                )
        )
                .isInstanceOf(
                        JogoRawgNaoEncontradoException.class
                )
                .hasMessage(
                        "Jogo não encontrado na RAWG: 999999"
                );

        servidor.verify();
    }
}
