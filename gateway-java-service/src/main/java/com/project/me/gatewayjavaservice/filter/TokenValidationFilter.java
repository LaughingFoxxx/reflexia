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
        if (path.startsWith("/api/auth")) {
            return chain.filter(exchange)
                    .doOnError(error -> log.info("Ошибка в Gateway: " + error.getMessage()))
                    .doOnSuccess(x -> log.info("Запрос успешно передан дальше"));
        }

        // Извлекаем access_token из cookies
        List<HttpCookie> cookies = exchange.getRequest().getCookies().get("access_token");
        String tokenValue;
        if (cookies == null || cookies.isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен отсутствует в cookies"));
        }
        tokenValue = cookies.getFirst().getValue(); // Берем первый access_token из cookies

        // Проверка токена в черном списке
        return reactiveRedisTemplate.hasKey("blacklist:" + tokenValue)
                .onErrorResume(e -> {
                    log.error("Ошибка подключения к Redis: {}", e.getMessage());
                    return Mono.just(false); // Пропускаем, если Redis недоступен
                })
                .flatMap(isBlacklisted -> {
                    log.info("Gateway-API-Service. Проверка JWT-токена в черном списке");
                    if (isBlacklisted) {
                        log.warn("Токен {} отклонен: находится в черном списке", tokenValue);
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен в черном списке"));
                    }

                    // Валидация токена через /auth/validate
                    return webClient.post()
                            .uri("/auth/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of("token", tokenValue))
                            .retrieve()
                            .bodyToMono(String.class)
                            .flatMap(email -> {
                                // Добавляем email в заголовок From
                                ServerHttpRequest request = exchange.getRequest().mutate()
                                        .header("From", email)
                                        .header("Authorization", "Bearer " + tokenValue) // Опционально: сохраняем для совместимости
                                        .header("X-Gateway-For", gatewayCode)
                                        .build();
                                return chain.filter(exchange.mutate().request(request).build());
                            })
                            .onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен")));
                });
    }

    @Override
    public int getOrder() {
        return -1; // Порядок выполнения фильтра (меньше = раньше)
    }
}