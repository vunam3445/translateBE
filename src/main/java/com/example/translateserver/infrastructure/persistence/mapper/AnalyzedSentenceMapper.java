package com.example.translateserver.infrastructure.persistence.mapper;

import com.example.translateserver.domain.model.AnalyzedSentence;
import com.example.translateserver.infrastructure.persistence.entity.AnalyzedSentenceEntity;
import com.example.translateserver.infrastructure.persistence.entity.ChapterEntity;

public class AnalyzedSentenceMapper {

    public static AnalyzedSentence toDomain(AnalyzedSentenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return AnalyzedSentence.builder()
                .id(entity.getId())
                .chapterId(entity.getChapter() != null ? entity.getChapter().getId() : null)
                .sentenceOrder(entity.getSentenceOrder())
                .viText(entity.getViText())
                .enText(entity.getEnText())
                .tense(entity.getTense())
                .wordsJsonMeta(entity.getWordsJsonMeta())
                .build();
    }

    public static AnalyzedSentenceEntity toEntity(AnalyzedSentence domain) {
        if (domain == null) {
            return null;
        }
        ChapterEntity chapterEntity = null;
        if (domain.getChapterId() != null) {
            chapterEntity = ChapterEntity.builder().id(domain.getChapterId()).build();
        }

        return AnalyzedSentenceEntity.builder()
                .id(domain.getId())
                .chapter(chapterEntity)
                .sentenceOrder(domain.getSentenceOrder())
                .viText(domain.getViText())
                .enText(domain.getEnText())
                .tense(domain.getTense())
                .wordsJsonMeta(domain.getWordsJsonMeta())
                .build();
    }
}
