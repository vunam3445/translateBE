package com.example.translateserver.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flashcard {
    private Long id;
    private String word;
    private String ipa;
    private String meaning;
    private String exampleEn;
    private String exampleVi;
    private int boxLevel;
    private LocalDate nextReviewDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
