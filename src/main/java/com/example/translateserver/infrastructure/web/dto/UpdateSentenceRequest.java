package com.example.translateserver.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSentenceRequest {
    @NotNull(message = "Nội dung câu dịch không được null")
    private String enText;
}
