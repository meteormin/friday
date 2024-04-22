package com.meteormin.friday;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.meteormin.friday.auth.domain.Token;
import com.meteormin.friday.common.fake.FakeInjector;
import com.meteormin.friday.infrastructure.config.SecurityConfiguration;
import com.meteormin.friday.infrastructure.security.auth.PasswordAuthentication;
import com.meteormin.friday.infrastructure.security.auth.response.PasswordTokenResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractAcceptanceTest {
    @LocalServerPort
    int port;

    protected static String accessToken;

    protected final ObjectMapper objectMapper;

    protected final FakeInjector fakeInjector;

    public AbstractAcceptanceTest() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        fakeInjector = new FakeInjector(
                new Faker(),
                objectMapper);
    }

    @BeforeEach
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    protected Token loginTest() {
        var req = new PasswordAuthentication(
                fakeInjector.getFaker().internet().safeEmailAddress(),
                fakeInjector.getFaker().internet().password(),
                "secret");

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(req)
                .when()
                .post(SecurityConfiguration.LOGIN_URL)
                .then().log().all()
                .extract();
        assertThat(response.statusCode()).isEqualTo(201);

        PasswordTokenResponse token = response
                .body().as(PasswordTokenResponse.class);

        assertThat(token).isNotNull()
                .hasFieldOrProperty("id")
                .hasFieldOrProperty("email")
                .hasFieldOrProperty("name")
                .hasFieldOrProperty("tokens.tokenType")
                .hasFieldOrProperty("tokens.accessToken")
                .hasFieldOrProperty("tokens.expiresIn")
                .hasFieldOrProperty("tokens.refreshToken");

        accessToken = token.tokens()
                .accessToken();

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(token.tokens().refreshToken())
                .tokenType(token.tokens().tokenType())
                .expiresIn(token.tokens().expiresIn())
                .build();
    }
}
