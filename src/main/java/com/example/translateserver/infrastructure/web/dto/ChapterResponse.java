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
public class ChapterResponse {
    private Long id;
    private Long storyId;
    private int chapterNumber;
    private String title;
    private String status;
    private String translatedContent;
    private int translationProgress;
    private LocalDateTime createdAt;
}
