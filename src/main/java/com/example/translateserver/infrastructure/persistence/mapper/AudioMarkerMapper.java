package com.example.translateserver.infrastructure.persistence.mapper;

import com.example.translateserver.domain.model.AudioMarker;
import com.example.translateserver.infrastructure.persistence.entity.AudioMarkerEntity;
import com.example.translateserver.infrastructure.persistence.entity.ChapterEntity;

public class AudioMarkerMapper {

    public static AudioMarker toDomain(AudioMarkerEntity entity) {
        if (entity == null) {
            return null;
        }
        return AudioMarker.builder()
                .id(entity.getId())
                .chapterId(entity.getChapter() != null ? entity.getChapter().getId() : null)
                .sentenceOrder(entity.getSentenceOrder())
                .startTimeMs(entity.getStartTimeMs())
                .endTimeMs(entity.getEndTimeMs())
                .build();
    }

    public static AudioMarkerEntity toEntity(AudioMarker domain) {
        if (domain == null) {
            return null;
        }
        ChapterEntity chapterEntity = null;
        if (domain.getChapterId() != null) {
            chapterEntity = ChapterEntity.builder().id(domain.getChapterId()).build();
        }

        return AudioMarkerEntity.builder()
                .id(domain.getId())
                .chapter(chapterEntity)
                .sentenceOrder(domain.getSentenceOrder())
                .startTimeMs(domain.getStartTimeMs())
                .endTimeMs(domain.getEndTimeMs())
                .build();
    }
}
