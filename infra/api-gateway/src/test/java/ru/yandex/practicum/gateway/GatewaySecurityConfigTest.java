package ru.yandex.practicum.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "app.security.users[0].username=ivan",
                "app.security.users[0].password=ivan",
                "app.security.users[0].roles[0]=USER",
                "app.security.users[1].username=anna",
                "app.security.users[1].password=anna",
                "app.security.users[1].roles[0]=ADMIN",
                "app.security.users[1].roles[1]=USER"
        })
@AutoConfigureWebTestClient
class GatewaySecurityConfigTest {
    private static final String IVAN_WORD = "ivan";
    private static final String ANNA_WORD = "anna";
    private static final String ORDERS_ROUTE = "/api/orders";
    private static final String PRODUCTS_ROUTE = "/api/products";
    private static final String UNKNOWN_ROUTE = "/api/unknown";
    private static final String ANY_ROUTE = "/**";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void catalogGet_isPublic() {
        webTestClient.get()
                .uri(PRODUCTS_ROUTE)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void orderCreate_withoutCredentials_isUnauthorized() {
        webTestClient.post()
                .uri(ORDERS_ROUTE)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void orderCreate_withUserCredentials_isOk() {
        webTestClient.post()
                .uri(ORDERS_ROUTE)
                .header("Authorization", basic(IVAN_WORD, IVAN_WORD))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void productWrite_withUserCredentials_isForbidden() {
        webTestClient.post()
                .uri(PRODUCTS_ROUTE)
                .header("Authorization", basic(IVAN_WORD, IVAN_WORD))
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.patch()
                .uri(PRODUCTS_ROUTE + "/1")
                .header("Authorization", basic(IVAN_WORD, IVAN_WORD))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void productWrite_withAdminCredentials_passesSecurity() {
        webTestClient.post()
                .uri(PRODUCTS_ROUTE)
                .header("Authorization", basic(ANNA_WORD, ANNA_WORD))
                .exchange()
                .expectStatus().isOk();

        webTestClient.patch()
                .uri(PRODUCTS_ROUTE + "/1")
                .header("Authorization", basic(ANNA_WORD, ANNA_WORD))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void orderGet_withUserCredentials_isForbidden() {
        webTestClient.get()
                .uri(ORDERS_ROUTE)
                .header("Authorization", basic(IVAN_WORD, IVAN_WORD))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void orderGet_withAdminCredentials_isOk() {
        webTestClient.get()
                .uri(ORDERS_ROUTE)
                .header("Authorization", basic(ANNA_WORD, ANNA_WORD))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unknownRoute_withAdminCredentials_isForbidden() {
        webTestClient.post()
                .uri(UNKNOWN_ROUTE)
                .header("Authorization", basic(ANNA_WORD, ANNA_WORD))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void corsPreflight_isPublic() {
        webTestClient.options()
                .uri(ORDERS_ROUTE)
                .exchange()
                .expectStatus().isOk();
    }

    private String basic(String username, String password) {
        String value = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    @TestConfiguration
    static class TestBackendConfig {

        @Bean
        RouterFunction<ServerResponse> testBackendRoutes() {
            return route()
                    .GET(PRODUCTS_ROUTE + ANY_ROUTE, request -> ServerResponse.ok().build())
                    .GET(ORDERS_ROUTE + ANY_ROUTE, request -> ServerResponse.ok().build())
                    .POST(PRODUCTS_ROUTE + ANY_ROUTE, request -> ServerResponse.ok().build())
                    .POST(ORDERS_ROUTE + ANY_ROUTE, request -> ServerResponse.ok().build())
                    .PATCH(PRODUCTS_ROUTE + ANY_ROUTE, request -> ServerResponse.ok().build())
                    .OPTIONS(ORDERS_ROUTE + ANY_ROUTE, request -> ServerResponse.ok().build())
                    .build();
        }
    }
} 