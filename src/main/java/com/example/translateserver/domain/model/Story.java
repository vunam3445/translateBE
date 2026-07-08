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
public class Story {
    private Long id;
    private String title;
    private int totalChapters;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
