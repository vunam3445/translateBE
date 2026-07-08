package com.example.translateserver.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGlossaryRequest {
    @NotBlank(message = "Tên tiếng Việt không được để trống")
    private String viTerm;

    @NotBlank(message = "Tên dịch tiếng Anh không được để trống")
    private String enTerm;
}
