package com.project.me.central_java_service.model.dto;

import jakarta.validation.constraints.NotNull;

public record TextResponseDTO(

        @NotNull
        String result
) {
}
