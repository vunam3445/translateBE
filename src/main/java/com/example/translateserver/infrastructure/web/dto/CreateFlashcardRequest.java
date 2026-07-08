package com.example.translateserver.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlashcardRequest {
    @NotBlank(message = "Từ vựng không được để trống")
    private String word;
    private String ipa;
    @NotBlank(message = "Nghĩa của từ không được để trống")
    private String meaning;
    private String exampleEn;
    private String exampleVi;
}
