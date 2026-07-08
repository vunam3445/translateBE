package com.example.translateserver.infrastructure.persistence.mapper;

import com.example.translateserver.domain.model.Chapter;
import com.example.translateserver.infrastructure.persistence.entity.ChapterEntity;
import com.example.translateserver.infrastructure.persistence.entity.StoryEntity;

public class ChapterMapper {

    public static Chapter toDomain(ChapterEntity entity) {
        if (entity == null) {
            return null;
        }
        return Chapter.builder()
                .id(entity.getId())
                .storyId(entity.getStory() != null ? entity.getStory().getId() : null)
                .chapterNumber(entity.getChapterNumber())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .rawContent(entity.getRawContent())
                .translatedContent(entity.getTranslatedContent())
                .translationProgress(entity.getTranslationProgress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ChapterEntity toEntity(Chapter domain) {
        if (domain == null) {
            return null;
        }
        StoryEntity storyEntity = null;
        if (domain.getStoryId() != null) {
            storyEntity = StoryEntity.builder().id(domain.getStoryId()).build();
        }
        
        return ChapterEntity.builder()
                .id(domain.getId())
                .story(storyEntity)
                .chapterNumber(domain.getChapterNumber())
                .title(domain.getTitle())
                .status(domain.getStatus())
                .rawContent(domain.getRawContent())
                .translatedContent(domain.getTranslatedContent())
                .translationProgress(domain.getTranslationProgress())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
