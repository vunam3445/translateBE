package com.example.translateserver.domain.repository;

import com.example.translateserver.domain.model.Chapter;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository {
    Chapter save(Chapter chapter);
    Optional<Chapter> findById(Long id);
    Optional<Chapter> findByStoryIdAndChapterNumber(Long storyId, int chapterNumber);
    List<Chapter> findByStoryId(Long storyId);
    void deleteById(Long id);
    Optional<Integer> findMaxChapterNumberByStoryId(Long storyId);
}
