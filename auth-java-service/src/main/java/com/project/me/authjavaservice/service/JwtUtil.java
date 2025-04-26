package com.project.me.authjavaservice.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.me.authjavaservice.exception.BaseAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    @Value("${auth.jwt.access-token-expiration}")
    private long accessExpiration;

    @Value("${auth.jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private final JWSSigner jwsSigner;
    private final JWSVerifier verifier;

    public JwtUtil(@Value("${auth.jwt.secret}") String secret) {
        try {
            this.jwsSigner = new MACSigner(secret.getBytes());
            this.verifier = new MACVerifier(secret);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

    }

    public String generateRefreshToken(String email) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .expirationTime(new Date(System.currentTimeMillis() + refreshExpiration))
                    .claim("type", "refresh")
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(jwsSigner);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.warn("JwtUtil. generateRefreshToken - ошибка подписи токена: {}", e.getMessage());
            throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Ошибка подписи токена");
        }
    }

    public String generateAccessToken(String email) {
        try {
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .expirationTime(new Date(System.currentTimeMillis() + accessExpiration))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaimsSet);
            signedJWT.sign(jwsSigner);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.warn("JwtUtil. generateAccessToken - ошибка подписи токена: {}", e.getMessage());
            throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Ошибка подписи токена");
        }
    }

    public String validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            if (!jwt.verify(verifier)) {
                throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Ошибка подписи токена");
            }

            JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
            if (jwtClaimsSet.getExpirationTime().before(new Date())) {
                throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Токен истёк");
            }

            return jwtClaimsSet.getSubject();

        } catch (ParseException | JOSEException e) {
            if (e instanceof ParseException) {
                log.warn("Ошибка парсинга token={}", token);
                throw new BaseAuthException(HttpStatus.BAD_REQUEST, "Ошибка парсинга токена");
            } else {
                log.warn("Ошибка подписи: token={}", token);
                throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Ошибка подписи токена");
            }
        }
    }

    public long getRemainingTTL(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            if (!signedJWT.verify(verifier)) {
                log.warn("JWTUtil. Ошибка при верификации токена");
                throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Ошибка подписи токена");
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            long now = System.currentTimeMillis();
            long ttlMillis = expiration.getTime() - now;
            if (ttlMillis <= 0) {
                log.warn("JWTUtil. Разница времени истечения и настоящего - отрицательная. Токен истёк");
                throw new BaseAuthException(HttpStatus.UNAUTHORIZED, "Токен истёк");
            }

            return ttlMillis / 1000;

        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
