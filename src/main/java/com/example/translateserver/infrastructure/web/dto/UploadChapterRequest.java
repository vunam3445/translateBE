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
public class UploadChapterRequest {

    @NotBlank(message = "Tiêu đề chương không được để trống")
    private String title;

    @NotBlank(message = "Nội dung chương không được để trống")
    private String content;
}
