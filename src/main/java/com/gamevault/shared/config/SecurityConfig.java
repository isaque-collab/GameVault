package com.gamevault.shared.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())

                .csrf(csrf ->
                        csrf.spa()
                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                .dispatcherTypeMatchers(
                                        DispatcherType.ERROR
                                ).permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/csrf"
                                ).permitAll()

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/usuarios"
                                ).permitAll()

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/jogos/**"
                                ).permitAll()

                                .anyRequest()
                                .authenticated()
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.setStatus(
                                                HttpServletResponse.SC_UNAUTHORIZED
                                        )
                        )
                )

                .formLogin(form ->
                        form
                                .loginPage(
                                        "/api/auth/login"
                                )
                                .loginProcessingUrl(
                                        "/api/auth/login"
                                )
                                .usernameParameter(
                                        "email"
                                )
                                .passwordParameter(
                                        "senha"
                                )
                                .successHandler(
                                        (request, response, authentication) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_NO_CONTENT
                                                )
                                )
                                .failureHandler(
                                        (request, response, exception) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_UNAUTHORIZED
                                                )
                                )
                                .permitAll()
                )

                .logout(logout ->
                        logout
                                .logoutUrl(
                                        "/api/auth/logout"
                                )
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies(
                                        "JSESSIONID"
                                )
                                .logoutSuccessHandler(
                                        (request, response, authentication) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_NO_CONTENT
                                                )
                                )
                                .permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${gamevault.cors.allowed-origin}")
            String allowedOrigin
    ) {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(allowedOrigin)
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Content-Type",
                        "X-XSRF-TOKEN"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/api/**",
                configuration
        );

        return source;
    }
}
