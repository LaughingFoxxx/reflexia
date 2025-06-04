package com.project.me.central_java_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TextRequestDTO(

        @NotNull
        @NotBlank
        @NotEmpty
        String text,

        @NotNull
        @NotBlank
        @NotEmpty
        String instruction
) {
}
