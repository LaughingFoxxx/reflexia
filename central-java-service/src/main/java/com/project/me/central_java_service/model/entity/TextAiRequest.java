package com.project.me.central_java_service.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TextAiRequest {
    private String aiRequestId;
    private String requestText;
    private String responseText;
    private String prompt;
    private LocalDateTime requestTime;
}
