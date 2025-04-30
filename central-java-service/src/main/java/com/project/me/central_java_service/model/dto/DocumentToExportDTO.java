package com.project.me.central_java_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DocumentToExportDTO(
        @NotNull
        String documentId,

        @NotNull
        @NotBlank
        @NotEmpty
        String fileName,

        @NotNull
        String format,

        @NotNull
        float leftMargin,

        @NotNull
        float rightMargin,

        @NotNull
        float topMargin,

        @NotNull
        float bottomMargin,

        @NotNull
        int fontSize,

        @NotNull
        String fontName,

        @NotNull
        float lineSpacing
) {
}
