package com.example.translateserver.infrastructure.web.dto;

import com.example.translateserver.domain.util.StringSanitizer;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoryRequest {
    @NotBlank(message = "Tên truyện không được để trống")
    private String title;

    public void setTitle(String title) {
        this.title = StringSanitizer.sanitize(title);
    }
}

