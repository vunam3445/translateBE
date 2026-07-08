package com.example.translateserver.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlossaryResponse {
    private Long id;
    private Long storyId;
    private String viTerm;
    private String enTerm;
    private boolean isManualAddition;
    private LocalDateTime createdAt;
}
