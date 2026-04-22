package com.dukaanpe.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayHealthIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void actuatorHealthIsReachable() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void expectedCoreRoutesAreLoaded() {
        List<String> routeIds = awaitRouteIds(Duration.ofSeconds(5));

        assertThat(routeIds).isNotNull();
        assertThat(routeIds).containsAll(Arrays.asList(
            "auth-service",
            "store-service",
            "product-inventory-service",
            "billing-pos-service",
            "udhar-khata-service",
            "supplier-purchase-service",
            "payment-service",
            "customer-loyalty-service",
            "gst-tax-service",
            "analytics-service",
            "notification-service",
            "language-service",
            "sync-offline-service"
        ));
        assertThat(routeIds).isNotEmpty();
    }

    @Test
    void corsConfigurationAllowsFrontendOriginForApiRoutes() {
        MockServerHttpRequest request = MockServerHttpRequest
            .options("http://localhost/api/auth/send-otp")
            .header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "POST")
            .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = e -> Mono.empty();

        corsWebFilter.filter(exchange, chain).block(Duration.ofSeconds(2));

        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin"))
            .isEqualTo("http://localhost:4200");
    }

    @Test
    void corsConfigurationDoesNotAllowUnknownOrigin() {
        MockServerHttpRequest request = MockServerHttpRequest
            .options("http://localhost/api/auth/send-otp")
            .header("Origin", "http://malicious.example")
            .header("Access-Control-Request-Method", "POST")
            .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = e -> Mono.empty();

        corsWebFilter.filter(exchange, chain).block(Duration.ofSeconds(2));

        assertThat(exchange.getResponse().getHeaders().getFirst("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    void unmatchedApiPathReturnsNotFound() {
        webTestClient.get()
            .uri("/api/does-not-exist")
            .exchange()
            .expectStatus().isNotFound();
    }

    private List<String> awaitRouteIds(Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            List<String> routeIds = routeLocator.getRoutes()
                .map(route -> route.getId())
                .collectList()
                .block(Duration.ofSeconds(2));

            if (routeIds != null && !routeIds.isEmpty()) {
                return routeIds;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return Collections.emptyList();
    }
}
