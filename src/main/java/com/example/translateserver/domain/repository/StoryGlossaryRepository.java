package com.example.translateserver.domain.repository;

import com.example.translateserver.domain.model.StoryGlossary;

import java.util.List;
import java.util.Optional;

public interface StoryGlossaryRepository {
    StoryGlossary save(StoryGlossary glossary);
    Optional<StoryGlossary> findById(Long id);
    List<StoryGlossary> findByStoryId(Long storyId);
    Optional<StoryGlossary> findByStoryIdAndViTerm(Long storyId, String viTerm);
    void deleteById(Long id);
}
