package com.example.translateserver.infrastructure.persistence.mapper;

import com.example.translateserver.domain.model.Story;
import com.example.translateserver.infrastructure.persistence.entity.StoryEntity;

public class StoryMapper {

    public static Story toDomain(StoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return Story.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .totalChapters(entity.getTotalChapters())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static StoryEntity toEntity(Story domain) {
        if (domain == null) {
            return null;
        }
        return StoryEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .totalChapters(domain.getTotalChapters())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
