package com.gamevault.rawg.client;

import com.gamevault.rawg.config.RawgConfig;
import com.gamevault.rawg.config.RawgProperties;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
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
}
