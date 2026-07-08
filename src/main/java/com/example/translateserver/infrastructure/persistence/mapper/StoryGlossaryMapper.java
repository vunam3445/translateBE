package com.example.translateserver.infrastructure.persistence.mapper;

import com.example.translateserver.domain.model.StoryGlossary;
import com.example.translateserver.infrastructure.persistence.entity.StoryEntity;
import com.example.translateserver.infrastructure.persistence.entity.StoryGlossaryEntity;

public class StoryGlossaryMapper {

    public static StoryGlossary toDomain(StoryGlossaryEntity entity) {
        if (entity == null) {
            return null;
        }
        return StoryGlossary.builder()
                .id(entity.getId())
                .storyId(entity.getStory() != null ? entity.getStory().getId() : null)
                .viTerm(entity.getViTerm())
                .enTerm(entity.getEnTerm())
                .description(entity.getDescription())
                .isManualAddition(entity.isManualAddition())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static StoryGlossaryEntity toEntity(StoryGlossary domain) {
        if (domain == null) {
            return null;
        }
        StoryEntity storyEntity = null;
        if (domain.getStoryId() != null) {
            storyEntity = StoryEntity.builder().id(domain.getStoryId()).build();
        }

        return StoryGlossaryEntity.builder()
                .id(domain.getId())
                .story(storyEntity)
                .viTerm(domain.getViTerm())
                .enTerm(domain.getEnTerm())
                .description(domain.getDescription())
                .isManualAddition(domain.isManualAddition())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
