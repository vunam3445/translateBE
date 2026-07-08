package com.example.translateserver.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhraseTranslateRequest {
    
    @NotBlank(message = "Cụm từ không được để trống")
    private String phrase;
    
    @NotBlank(message = "Ngữ cảnh của câu không được để trống")
    private String context;
}
