package com.project.me.central_java_service.dto;

import jakarta.validation.constraints.NotNull;

public record SaveDocumentDTO(

        @NotNull
        String documentId,

        @NotNull
        String documentName,

        @NotNull
        String text
) {
}
