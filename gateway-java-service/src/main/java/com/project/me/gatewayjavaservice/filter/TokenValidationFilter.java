package com.project.me.gatewayjavaservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class TokenValidationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TokenValidationFilter.class);
    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final String gatewayCode;

    @Autowired
    public TokenValidationFilter(WebClient.Builder webClientBuilder,
                                 ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
                                 @Value("${service.code}") String gatewayCode
    ) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8083").build();
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.gatewayCode = gatewayCode;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("Gateway-API-Service. Запрос по адресу: {}", path);
        log.info("Gateway-API-Service. Cookies в запросе: {}", exchange.getRequest().getCookies());

        // Add X-Gateway-For header to all requests at the start
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Gateway-For", gatewayCode)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(request).build();

        // Skip token validation for /api/auth paths
        if (path.startsWith("/api/auth")) {
            return chain.filter(mutatedExchange)
                    .doOnError(error -> log.info("Ошибка в Gateway: " + error.getMessage()))
                    .doOnSuccess(x -> log.info("Запрос успешно передан дальше"));
        }

        // Extract access_token from cookies
        List<HttpCookie> cookies = exchange.getRequest().getCookies().get("access_token");
        if (cookies == null || cookies.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен отсутствует в cookies"));
        }
        String tokenValue = cookies.getFirst().getValue();

        // Check token in blacklist
        return reactiveRedisTemplate.hasKey("blacklist:" + tokenValue)
                .onErrorResume(e -> {
                    log.error("Ошибка подключения к Redis: {}", e.getMessage());
                    return Mono.just(false); // Proceed if Redis is unavailable
                })
                .flatMap(isBlacklisted -> {
                    log.info("Gateway-API-Service. Проверка JWT-токена в черном списке");
                    if (isBlacklisted) {
                        log.warn("Токен {} отклонен: находится в черном списке", tokenValue);
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен в черном списке"));
                    }

                    // Validate token via /auth/validate
                    return webClient.post()
                            .uri("/auth/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("token", tokenValue))
                            .header("X-Gateway-For", gatewayCode) // Ensure header is passed to validation service
                            .retrieve()
                            .bodyToMono(String.class)
                            .flatMap(email -> {
                                // Add From and Authorization headers
                                ServerHttpRequest validatedRequest = mutatedExchange.getRequest().mutate()
                                        .header("From", email)
                                        .header("Authorization", "Bearer " + tokenValue)
                                        .build();
                                return chain.filter(mutatedExchange.mutate().request(validatedRequest).build());
                            })
                            .onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен")));
                });
    }

    @Override
    public int getOrder() {
        return -1; // Порядок выполнения фильтра (меньше = раньше)
    }
}