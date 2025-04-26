package com.project.me.central_java_service.model.dto;

import jakarta.validation.constraints.NotNull;

public record TextRequestDTO(

        @NotNull
        String text,

        @NotNull
        String instruction
) {
}
