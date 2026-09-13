package com.gamevault.rawg.client;

import com.gamevault.rawg.config.RawgConfig;
import com.gamevault.rawg.dto.RawgJogoDetalhesResposta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RawgClient.class)
@Import(RawgConfig.class)
@TestPropertySource(properties = {
        "rawg.base-url=https://api.rawg.io/api",
        "rawg.api-key=chave-teste"
})
class RawgClientTest {

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
}
