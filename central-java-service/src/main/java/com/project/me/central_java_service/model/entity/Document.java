package com.project.me.central_java_service.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Document {
    private String documentId;

    private String documentName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String text;
}
