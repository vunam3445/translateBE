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
public class StoryGlossary {
    private Long id;
    private Long storyId;
    private String viTerm;
    private String enTerm;
    private String description;
    private boolean isManualAddition;
    private LocalDateTime createdAt;
}
