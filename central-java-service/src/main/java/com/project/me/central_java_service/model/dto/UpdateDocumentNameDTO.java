package com.project.me.central_java_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateDocumentNameDTO(
        @NotNull
        @NotBlank
        @NotEmpty
        String name,

        @NotNull
        @NotBlank
        @NotEmpty
        String documentId
) {
}
