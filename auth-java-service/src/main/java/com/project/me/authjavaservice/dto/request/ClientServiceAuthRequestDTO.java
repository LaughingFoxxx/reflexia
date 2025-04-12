package com.project.me.authjavaservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record ClientServiceAuthRequestDTO(
        @NotNull
        String clientId,

        @NotNull
        String name
) {
}
