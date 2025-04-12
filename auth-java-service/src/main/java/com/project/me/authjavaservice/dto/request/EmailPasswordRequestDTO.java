package com.project.me.authjavaservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EmailPasswordRequestDTO(
        @NotNull
        @NotEmpty
        @NotBlank
        @Email
        String email,

        @NotNull
        @NotEmpty
        @NotBlank
        String password
) {}
