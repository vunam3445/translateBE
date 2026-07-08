package com.example.translateserver.infrastructure.persistence.repository;

import com.example.translateserver.infrastructure.persistence.entity.AnalyzedSentenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataAnalyzedSentenceRepository extends JpaRepository<AnalyzedSentenceEntity, Long> {
    List<AnalyzedSentenceEntity> findByChapterId(Long chapterId);
    Optional<AnalyzedSentenceEntity> findByChapterIdAndSentenceOrder(Long chapterId, int sentenceOrder);
    void deleteByChapterId(Long chapterId);
}
