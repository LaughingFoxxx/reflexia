package com.project.me.authjavaservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "verification_code")
    private String verificationCode;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
