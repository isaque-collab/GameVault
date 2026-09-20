package com.gamevault.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigIntegracaoTest {

    private static final String ORIGEM_PERMITIDA =
            "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePermitirRequisicaoDaOrigemDoFrontend()
            throws Exception {

        mockMvc.perform(
                        options("/api/usuarios/me")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        ORIGEM_PERMITIDA
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                ORIGEM_PERMITIDA
                        )
                )
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                                "true"
                        )
                );
    }

    @Test
    void devePermitirCabecalhoCsrfDoFrontend()
            throws Exception {

        mockMvc.perform(
                        options("/api/usuarios/me")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        ORIGEM_PERMITIDA
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "PUT"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                        "Content-Type, X-XSRF-TOKEN"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                ORIGEM_PERMITIDA
                        )
                )
                .andExpect(
                        header().exists(
                                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS
                        )
                );
    }

    @Test
    void deveRejeitarOrigemNaoPermitida()
            throws Exception {

        mockMvc.perform(
                        options("/api/usuarios/me")
                                .header(
                                        HttpHeaders.ORIGIN,
                                        "http://origem-nao-permitida.test"
                                )
                                .header(
                                        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                                        "GET"
                                )
                )
                .andExpect(status().isForbidden());
    }
}
