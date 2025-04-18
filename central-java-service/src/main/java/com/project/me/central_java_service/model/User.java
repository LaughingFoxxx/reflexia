package com.project.me.central_java_service.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    String id;

    @Indexed
    String userEmail;

    private List<com.project.me.central_java_service.model.Document> documents;
}
