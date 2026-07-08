package com.example.translateserver.infrastructure.persistence.mapper;

import com.example.translateserver.domain.model.Flashcard;
import com.example.translateserver.infrastructure.persistence.entity.FlashcardEntity;

public class FlashcardMapper {

    public static Flashcard toDomain(FlashcardEntity entity) {
        if (entity == null) {
            return null;
        }
        return Flashcard.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .ipa(entity.getIpa())
                .meaning(entity.getMeaning())
                .exampleEn(entity.getExampleEn())
                .exampleVi(entity.getExampleVi())
                .boxLevel(entity.getBoxLevel())
                .nextReviewDate(entity.getNextReviewDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static FlashcardEntity toEntity(Flashcard domain) {
        if (domain == null) {
            return null;
        }
        return FlashcardEntity.builder()
                .id(domain.getId())
                .word(domain.getWord())
                .ipa(domain.getIpa())
                .meaning(domain.getMeaning())
                .exampleEn(domain.getExampleEn())
                .exampleVi(domain.getExampleVi())
                .boxLevel(domain.getBoxLevel())
                .nextReviewDate(domain.getNextReviewDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
