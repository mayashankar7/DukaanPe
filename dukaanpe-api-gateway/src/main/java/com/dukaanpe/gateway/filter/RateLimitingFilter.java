package com.dukaanpe.gateway.filter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_SECOND = 100;
    private static final long WINDOW_MILLIS = 1000;

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";

        long now = Instant.now().toEpochMilli();
        windowStart.putIfAbsent(clientIp, now);
        requestCounts.putIfAbsent(clientIp, new AtomicInteger(0));

        long start = windowStart.get(clientIp);
        if (now - start >= WINDOW_MILLIS) {
            windowStart.put(clientIp, now);
            requestCounts.get(clientIp).set(0);
        }

        int count = requestCounts.get(clientIp).incrementAndGet();
        if (count > MAX_REQUESTS_PER_SECOND) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}

