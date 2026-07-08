package com.example.translateserver.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {
    private Long id;
    private Long storyId;
    private int chapterNumber;
    private String title;
    private ChapterStatus status;
    private String rawContent;
    private String translatedContent;
    @Builder.Default
    private int translationProgress = 0;
    private LocalDateTime createdAt;
}

