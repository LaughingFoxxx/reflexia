package com.project.me.authjavaservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.me.authjavaservice.exception.*;
import com.project.me.authjavaservice.model.User;
import com.project.me.authjavaservice.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.project.me.authjavaservice.util.AuthUtil.hideEmail;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklistService;
    private final EmailService emailService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       TokenBlacklistService blacklistService,
                       EmailService emailService,
                       KafkaTemplate<String, String> kafkaTemplate,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
        this.emailService = emailService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean register(String email, String password) {
        log.info("AuthService. Регистрация: email={}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("AuthService. Пользователь с email={} уже существует", hideEmail(email));
            throw new BaseAuthException(HttpStatus.CONFLICT, "Пользователь уже существует");
        }

        User user = new User(email, passwordEncoder.encode(password));

        String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-значный код

        log.info("AuthService. Код подтверждения: \"{}\"", verificationCode);

        user.setVerificationCode(verificationCode);
        boolean res = emailService.sendVerificationCode(email, verificationCode);

        userRepository.save(user);
        return res;
    }

    public void verifyEmail(String email, String verificationCode) {
        log.info("AuthService. Подтверждение email: email={}", hideEmail(email));
        User user = getUser(email);

        if (user.isVerified()) {
            log.warn("AuthService. email={} уже подтвержден", hideEmail(email));
            throw new RuntimeException("Email уже подтвержден");
        }

        if (!verificationCode.equals(user.getVerificationCode())) {
            log.warn("AuthService. Код подтверждения code={} - неверен", verificationCode);
            throw new RuntimeException("Неверный код");
        }

        user.setVerified(true);
        user.setVerificationCode(null);

        userRepository.save(user);
        log.info("AuthService. Пользователь с email={} успешно подтвердил адрес электронной почты", hideEmail(email));

        log.info("AuthService. Отправляем запрос на создание пользователя на Core-сервис для пользователя {} через топик \"new-user\"", email);
        try {
            kafkaTemplate.send("new-user", objectMapper.writeValueAsString(Map.of("email", hideEmail(email))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void login(String email, String password, HttpServletResponse response) {
        log.info("AuthService. Вход в приложение: email={}", hideEmail(email));
        User user = getUser(email);

        if (!user.isVerified()) {
            log.warn("AuthService. Email={} не подтвержден", hideEmail(email));
            throw new RuntimeException("Подтвердите email перед входом");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("AuthService. Неверный пароль для email={}", hideEmail(email));
            throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Неверный пароль");
        }

        log.info("AuthService. refresh_токен не найден, генерируем токены");
        String refreshToken = jwtUtil.generateRefreshToken(email);
        String accessToken = jwtUtil.generateAccessToken(email);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        log.debug("Устанавливаемый access_token: {}", accessToken);
        log.debug("Устанавливаемый refresh_token: {}", refreshToken);

        setAccessTokenCookie(accessToken, response);
        setRefreshTokenCookie(refreshToken, response);


        log.info("AuthService. Успешный вход в приложение для email={}", hideEmail(email));
    }

    public void refreshAccessToken(String refreshToken, HttpServletResponse response) {
        log.info("AuthService. Обновление токена: token={}", refreshToken);
        String email = jwtUtil.validateToken(refreshToken);

        User user = getUser(email);

        if (!refreshToken.equals(user.getRefreshToken())) {
            log.warn("AuthService. Неверный refresh_token={} для email={}", refreshToken, hideEmail(email));
            throw new RuntimeException("Неверный refresh_token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(email);

        log.info("AuthService. Токен обновлен, новый токен: token={}", newAccessToken);

        setAccessTokenCookie(newAccessToken, response);
    }

    public void logout(String token, HttpServletResponse response) {
        log.info("AuthService. Запрос на выход из приложения");

        log.info("AuthService. Добававление запрошенного access_токена в черный список...");
        String email = jwtUtil.validateToken(token);

        long accessTokenExpirationTime = jwtUtil.getRemainingTTL(token);

        if (!blacklistService.isTokenInBlacklist(token)) {
            blacklistService.addTokenToRedisBlacklist(token, accessTokenExpirationTime);
            log.info("AuthService. access_token добавлен в черный список");
        } else {
            log.info("AuthService. access_токен уже в черном списке. Всё равно будет удален у пользователя");
        }

        log.info("AuthService. Поиск и добавление refresh_токена в черный список...");
        User user = getUser(email);
        long refreshTokenExpirationTime = jwtUtil.getRemainingTTL(user.getRefreshToken());
        if (!blacklistService.isTokenInBlacklist(user.getRefreshToken())) {
            log.info("AuthService. refresh_token добавлен в черный список");
            blacklistService.addTokenToRedisBlacklist(user.getRefreshToken(), refreshTokenExpirationTime);
        } else {
            log.info("AuthService. refresh_токен уже в черном списке. Всё равно будет удален у пользователя");
        }
        user.setRefreshToken(null);
        userRepository.save(user);

        setCookieMaxAgeToZeroForAccessToken(response);
        setCookieMaxAgeToZeroForRefreshToken(response);
    }

    public void resetUserForPasswordChange(String email) {
        log.info("AuthService. Начинается смена пароля для email={}", hideEmail(email));
        User user = getUser(email);
        String verificationCode = String.valueOf((int) (Math.random() * 900000) + 100000); // 6-значный код
        user.setVerified(false);
        user.setVerificationCode(verificationCode);
        emailService.sendVerificationCode(email, verificationCode);
        log.info("AuthService. Письмо с подтверждением отправлено на email пользователя");
        userRepository.save(user);
    }

    public void setNewPasswordForUser(String email, String password) {
        log.info("AuthService. Установка нового пароля для email={}", hideEmail(email));
        User user = getUser(email);
        user.setPassword(passwordEncoder.encode(password));
        log.info("AuthService. Новый пароль установлен для пользователя email={}", hideEmail(email));
        userRepository.save(user);
    }

    private void setAccessTokenCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60 * 1000);
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void setCookieMaxAgeToZeroForAccessToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void setCookieMaxAgeToZeroForRefreshToken(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private User getUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(
                        () -> new BaseAuthException(HttpStatus.NOT_FOUND, "Пользователь не найден")
                );
    }
}