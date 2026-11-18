package com.umadjo.api.infra.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.umadjo.api.domain.user.entity.User;
import com.umadjo.api.infra.exception.exceptions.TokenJwtException;

class TokenServiceTest {

    private static final String SECRET = "segredo-de-teste-com-tamanho-suficiente-para-hmac256";
    private static final String ISSUER = "caixa-umadjo-api";

    private TokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new TokenService();
        ReflectionTestUtils.setField(service, "SECRET_API_TOKEN", SECRET);
        user = new User();
        user.setEmail("fulano@teste.com");
    }

    @Test
    @DisplayName("token gerado é validado e devolve o e-mail do usuário")
    void generateAndValidateRoundTrip() {
        String token = service.generateToken(user);

        assertThat(service.validateToken(token)).isEqualTo("fulano@teste.com");
    }

    @Test
    @DisplayName("token usa HS256, issuer da API, subject = e-mail e expira em 15 dias")
    void tokenClaims() {
        DecodedJWT decoded = JWT.decode(service.generateToken(user));

        assertThat(decoded.getAlgorithm()).isEqualTo("HS256");
        assertThat(decoded.getIssuer()).isEqualTo(ISSUER);
        assertThat(decoded.getSubject()).isEqualTo("fulano@teste.com");
        assertThat(Duration.between(Instant.now(), decoded.getExpiresAtAsInstant()))
                .isBetween(Duration.ofDays(15).minusMinutes(1), Duration.ofDays(15));
    }

    @Test
    @DisplayName("texto que não é JWT lança TokenJwtException")
    void garbageToken() {
        assertThatThrownBy(() -> service.validateToken("nao-e-um-jwt")).isInstanceOf(TokenJwtException.class);
    }

    @Test
    @DisplayName("token assinado com outro segredo lança TokenJwtException")
    void tokenSignedWithAnotherSecret() {
        String token = JWT.create().withIssuer(ISSUER).withSubject("fulano@teste.com")
                .withExpiresAt(Instant.now().plusSeconds(600))
                .sign(Algorithm.HMAC256("outro-segredo-qualquer-com-tamanho-razoavel"));

        assertThatThrownBy(() -> service.validateToken(token)).isInstanceOf(TokenJwtException.class);
    }

    @Test
    @DisplayName("token expirado lança TokenJwtException")
    void expiredToken() {
        String token = JWT.create().withIssuer(ISSUER).withSubject("fulano@teste.com")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SECRET));

        assertThatThrownBy(() -> service.validateToken(token)).isInstanceOf(TokenJwtException.class);
    }

    @Test
    @DisplayName("token de outro issuer lança TokenJwtException")
    void tokenFromAnotherIssuer() {
        String token = JWT.create().withIssuer("outra-api").withSubject("fulano@teste.com")
                .withExpiresAt(Instant.now().plusSeconds(600))
                .sign(Algorithm.HMAC256(SECRET));

        assertThatThrownBy(() -> service.validateToken(token)).isInstanceOf(TokenJwtException.class);
    }
}
