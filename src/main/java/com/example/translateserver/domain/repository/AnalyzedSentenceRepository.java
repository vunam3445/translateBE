package com.example.translateserver.domain.repository;

import com.example.translateserver.domain.model.AnalyzedSentence;

import java.util.List;
import java.util.Optional;

public interface AnalyzedSentenceRepository {
    AnalyzedSentence save(AnalyzedSentence sentence);
    List<AnalyzedSentence> saveAll(List<AnalyzedSentence> sentences);
    List<AnalyzedSentence> findByChapterId(Long chapterId);
    Optional<AnalyzedSentence> findByChapterIdAndSentenceOrder(Long chapterId, int sentenceOrder);
    void deleteByChapterId(Long chapterId);
}
