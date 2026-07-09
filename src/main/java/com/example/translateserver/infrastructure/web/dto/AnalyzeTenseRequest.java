package com.example.translateserver.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhận yêu cầu phân tích thì cho một câu tiếng Anh đơn lẻ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeTenseRequest {

    @NotBlank(message = "Câu tiếng Anh không được để trống")
    private String sentence;
}
