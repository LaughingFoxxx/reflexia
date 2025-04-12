package com.project.me.authjavaservice.controller;

import com.project.me.authjavaservice.dto.request.EmailPasswordRequestDTO;
import com.project.me.authjavaservice.service.AuthService;
import com.project.me.authjavaservice.service.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // Регистрация аккаунта
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid EmailPasswordRequestDTO requestDTO) {
        log.info("AuthController. POST-запрос. Регистрация: email={}", requestDTO.email());

        authService.register(requestDTO.email(), requestDTO.password());

        return ResponseEntity.ok("Регистрация прошла успешно");
    }

    // Вход в аккаунт
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login (@RequestBody @Valid EmailPasswordRequestDTO requestDTO, HttpServletResponse response) {
        log.info("AuthController. POST-запрос. Логин: email={}", requestDTO.email());

        authService.login(requestDTO.email(), requestDTO.password(), response);

        return ResponseEntity.ok(Map.of("message", "Успешный вход"));
    }

    // Валидация access токена на Gateway
    @PostMapping(value = "/validate")
    public ResponseEntity<String> validateTokenForGateway(@RequestBody Map<String, String> mapAccessToken) {
        log.info("AuthController. POST-запрос. Валидация access_токена по запросу от Gateway: {}", mapAccessToken.get("token"));

        String email = jwtUtil.validateToken(mapAccessToken.get("token"));

        if (email != null) {
            return ResponseEntity.ok(email);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    // Валидация access токена
    @PostMapping("/validate-access-token")
    public ResponseEntity<Map<String, String>> validateAccessToken(@CookieValue(value = "access_token") String accessToken) {
        log.info("AuthController. POST-запрос. Валидация access_токена: {}", accessToken);

        String email = jwtUtil.validateToken(accessToken);

        if (email != null) {
            return ResponseEntity.ok(Map.of("valid", "true"));
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    // Верификация почты
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        log.info("AuthController. POST-запрос. Верификация аккаунта по коду из почты: email={}, code={}",
                request.get("email"), request.get("code"));

        authService.verifyEmail(request.get("email"), request.get("code"));

        return ResponseEntity.ok("Email успешно подтверждён");
    }

    // Обновить токен
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessTokenWithRefreshToken(@CookieValue(value = "refresh_token") String refreshToken, HttpServletResponse response) {
        log.info("AuthController. POST-запрос. Обновление по refresh_token={}", refreshToken);

        authService.refreshAccessToken(refreshToken, response);

        return ResponseEntity.ok(Map.of("message", "Успешно получен новый access_token"));
    }

    // Выход из аккаунта
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue(value = "access_token") String authHeader, HttpServletResponse response) {
        log.info("AuthController. POST-запрос. Выход из приложения.");

        authService.logout(authHeader, response);

        return ResponseEntity.ok("Выход выполнен успешно");
    }

    // Запрос на выход из аккаунта
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        log.info("AuthController. POST-запрос. Запрос на смену пароля");

        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            log.warn("AuthController. Смена пароля отклонена. Email и/или пароль не могут быть пустыми");
            return ResponseEntity.badRequest().build();
        }

        authService.resetUserForPasswordChange(email);

        return ResponseEntity.ok("Запрос на смену пароля принят. Проверьте email");
    }

    // Установка нового пароля при сбросе пароля
    @PostMapping("/get-new-password")
    public ResponseEntity<String> getNewPassword(@RequestBody @Valid EmailPasswordRequestDTO requestDTO) {
        log.info("AuthController. POST-запрос. Новый пароль для пользователя");

        String email = requestDTO.email();
        String newPassword = requestDTO.password();

        if (email == null || email.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            log.warn("AuthController. Установка нового пароля отклонена. Email и/или пароль не могут быть пустыми");
            return ResponseEntity.badRequest().build();
        }

        authService.setNewPasswordForUser(email, newPassword);
        return ResponseEntity.ok("Пароль успешно сменен. Войдите в аккаунт");
    }

//    @PostMapping("/service-auth-token")
//    public ResponseEntity<?> getAccessTokenForService(@RequestBody ClientServiceAuthRequestDTO requestDTO) {
//        log.info("AuthController. Запрос от сервиса {} на получение access_токена", requestDTO.clientId());
//        authService.
//        return ResponseEntity.ok()
//    }
//
//    @GetMapping("/check")
//    public ResponseEntity<?> checkCookiesAuthorization(@CookieValue(value = "access_token") String accessToken) {
//        String email = jwtUtil.validateToken(accessToken);
//        if (email == null) {
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        } else {
//            return new ResponseEntity<>(HttpStatus.OK);
//        }
//    }
//
    // Верификация почты при сбросе пароля
//    @PostMapping("/verify-code")
//    public ResponseEntity<Map<String, String>> verifyEmailReset(@RequestBody Map<String, String> request) {
//        log.info("AuthController. POST-запрос. Верификация аккаунта по коду из почты для сброса пароля: email={}, code={}",
//                request.get("email"), request.get("code"));
//        authService.verifyEmail(request.get("email"), request.get("code"));
//        return ResponseEntity.ok(Map.of("type", "reset"));
//    }
// Валидация refresh токена
    //@PostMapping("/validate-refresh-token")
    //public ResponseEntity<Map<String, String>> validateRefreshToken(@CookieValue(value = "refresh_token") String refreshToken) {
    //    log.info("AuthController. POST-запрос. Валидация refresh_токена: {}", refreshToken);
    //
    //    String email = jwtUtil.validateToken(refreshToken);
    //
    //    if (email != null) {
    //        return ResponseEntity.ok(Map.of("valid", "true"));
    //    }
    //
    //    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    //}
}
