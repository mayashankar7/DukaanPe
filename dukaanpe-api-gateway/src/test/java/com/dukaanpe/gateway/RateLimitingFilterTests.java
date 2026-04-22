package com.dukaanpe.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.dukaanpe.gateway.filter.RateLimitingFilter;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

class RateLimitingFilterTests {

    @Test
    void blocksRequestsAfterPerSecondThreshold() {
        RateLimitingFilter filter = new RateLimitingFilter();
        AtomicInteger passed = new AtomicInteger(0);
        GatewayFilterChain chain = exchange -> {
            passed.incrementAndGet();
            return Mono.empty();
        };

        for (int i = 0; i < 100; i++) {
            MockServerWebExchange exchange = newExchange("127.0.0.1", 9000 + i);
            filter.filter(exchange, chain).block(Duration.ofSeconds(1));
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }

        MockServerWebExchange throttled = newExchange("127.0.0.1", 9101);
        filter.filter(throttled, chain).block(Duration.ofSeconds(1));

        assertThat(throttled.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(passed.get()).isEqualTo(100);
    }

    private MockServerWebExchange newExchange(String ip, int port) {
        MockServerHttpRequest request = MockServerHttpRequest
            .get("http://localhost/api/auth/send-otp")
            .remoteAddress(new InetSocketAddress(ip, port))
            .build();
        return MockServerWebExchange.from(request);
    }
}

