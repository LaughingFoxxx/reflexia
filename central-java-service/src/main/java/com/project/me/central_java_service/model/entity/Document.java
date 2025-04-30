package com.project.me.central_java_service.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Document implements Comparable<Document>{
    private String documentId;

    private String documentName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String text;

    @Override
    public int compareTo(Document other) {
        if (this.updatedAt == null && other.updatedAt == null) {
            return 0;
        }
        if (this.updatedAt == null) {
            return 1; // null считается "раньше"
        }
        if (other.updatedAt == null) {
            return -1; // null считается "раньше"
        }
        return other.updatedAt.compareTo(this.updatedAt); // От поздней к ранней
    }
}
