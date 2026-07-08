package com.example.translateserver.infrastructure.persistence.repository;

import com.example.translateserver.infrastructure.persistence.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataChapterRepository extends JpaRepository<ChapterEntity, Long> {
    List<ChapterEntity> findByStoryId(Long storyId);
    Optional<ChapterEntity> findByStoryIdAndChapterNumber(Long storyId, int chapterNumber);

    @org.springframework.data.jpa.repository.Query("SELECT MAX(c.chapterNumber) FROM ChapterEntity c WHERE c.story.id = :storyId")
    Optional<Integer> findMaxChapterNumberByStoryId(@org.springframework.data.repository.query.Param("storyId") Long storyId);
}
