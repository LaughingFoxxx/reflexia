package com.project.me.central_java_service.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    String id;

    @Indexed
    String userEmail;

    private List<com.project.me.central_java_service.model.entity.Document> documents;

    private List<TextAiRequest> requests;

    public List<com.project.me.central_java_service.model.entity.Document> getDocuments() {
        Collections.sort(this.documents); // Убедимся, что список отсортирован
        return this.documents;
    }
}
